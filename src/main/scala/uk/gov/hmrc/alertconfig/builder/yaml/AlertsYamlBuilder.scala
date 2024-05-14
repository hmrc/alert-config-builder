/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.alertconfig.builder.yaml

import uk.gov.hmrc.alertconfig.builder.GrafanaMigration.isGrafanaEnabled
import uk.gov.hmrc.alertconfig.builder._
import uk.gov.hmrc.alertconfig.builder.yaml.YamlWriter.mapper

import java.io.File

object AlertsYamlBuilder {
  import AppConfigValidator._
  val logger = new Logger()

  def run(alertConfigs: Seq[AlertConfig], environment: String): Unit = {
    val currentEnvironment = Environment.get(environment)
    val topLevelConfig = TopLevelConfig(convert(alertConfigs, currentEnvironment))
    logger.debug(s"Generating alert config YAML for $currentEnvironment")

    mapper.writeValue(new File(s"./target/output/services.yml"), topLevelConfig)
    logger.debug(s"Done generating alert config YAML for $currentEnvironment")
  }

  def convert(alertConfigs: Seq[AlertConfig], currentEnvironment: Environment): Seq[ServiceConfig] = {
    alertConfigs.flatMap(convert(_, currentEnvironment))
  }

  def convert(alertConfig: AlertConfig, currentEnvironment: Environment): Seq[ServiceConfig] = {
    val filtered = alertConfig.environmentConfig.filter(_.enabledEnvironments.contains(currentEnvironment))
    val enabledIntegrationsInEnv = filtered.map(_.integrationName).toSet
    val integrationSeveritiesForEnv = filtered
      .map(builder => builder.integrationName -> builder.enabledEnvironments.getOrElse(currentEnvironment, Set()))
      .toMap

    alertConfig.alertConfig.flatMap(convert(_, enabledIntegrationsInEnv, currentEnvironment, integrationSeveritiesForEnv))
  }

  def convert(alertConfigBuilder: AlertConfigBuilder, environmentDefinedIntegrations: Set[String], currentEnvironment: Environment, integrationSeveritiesForEnv: Map[String, Set[Severity]]): Option[ServiceConfig] = {
    val enabledIntegrations = alertConfigBuilder.integrations.toSet.intersect(environmentDefinedIntegrations)
    if (enabledIntegrations.isEmpty || !serviceDeployedInEnv(alertConfigBuilder.serviceName, alertConfigBuilder.platformService)) {
      None
    } else {
      val finalAlertConfigBuilder = removeUnusedAlerts(alertConfigBuilder, integrationSeveritiesForEnv)
      Some(
        ServiceConfig(
          service   = finalAlertConfigBuilder.serviceName.trim.toLowerCase.replaceAll(" ", "-"),
          alerts    = convertAlerts(finalAlertConfigBuilder, currentEnvironment),
          pagerduty = enabledIntegrations.map(integration => PagerDuty(integrationKeyName = integration)).toSeq
        ))
    }
  }

  def removeUnusedAlerts(alertConfigBuilder: AlertConfigBuilder, integrationSeveritiesForEnv: Map[String, Set[Severity]]): AlertConfigBuilder = {
    val uniqueEnabledSeveritiesForServiceInEnv = integrationSeveritiesForEnv.values.flatten.toSet
      if ( uniqueEnabledSeveritiesForServiceInEnv.contains(Severity.Critical) && !uniqueEnabledSeveritiesForServiceInEnv.contains(Severity.Warning)) {
        removeUnusedAlerts(alertConfigBuilder, AlertSeverity.Warning)
      } else if (
        uniqueEnabledSeveritiesForServiceInEnv.contains(Severity.Warning) && !uniqueEnabledSeveritiesForServiceInEnv.contains(Severity.Critical)
      ) {
        removeUnusedAlerts(alertConfigBuilder, AlertSeverity.Critical)
      } else {
        alertConfigBuilder
      }
  }

  def removeUnusedAlerts(alertConfigBuilder: AlertConfigBuilder, severityToRemove: AlertSeverity): AlertConfigBuilder =
    alertConfigBuilder.copy(
      exceptionThreshold                                  = if(alertConfigBuilder.exceptionThreshold.severity      == severityToRemove) ExceptionThreshold(count = Int.MaxValue)           else alertConfigBuilder.exceptionThreshold,
      http5xxThreshold                                    = if(alertConfigBuilder.http5xxThreshold.severity        == severityToRemove) Http5xxThreshold(count = Int.MaxValue)             else alertConfigBuilder.http5xxThreshold,
      http5xxPercentThreshold                             = if(alertConfigBuilder.http5xxPercentThreshold.severity == severityToRemove) Http5xxPercentThreshold(percentage = Int.MaxValue) else alertConfigBuilder.http5xxPercentThreshold,
      http90PercentileResponseTimeThresholds              = alertConfigBuilder.http90PercentileResponseTimeThresholds             .map(threshold => if(severityToRemove == AlertSeverity.Warning) threshold.copy(warning = None) else if (severityToRemove == AlertSeverity.Critical) threshold.copy(critical = None) else threshold),
      httpAbsolutePercentSplitThresholds                  = alertConfigBuilder.httpAbsolutePercentSplitThresholds                 .filterNot(_.severity == severityToRemove),
      httpAbsolutePercentSplitDownstreamServiceThresholds = alertConfigBuilder.httpAbsolutePercentSplitDownstreamServiceThresholds.filterNot(_.severity == severityToRemove),
      httpAbsolutePercentSplitDownstreamHodThresholds     = alertConfigBuilder.httpAbsolutePercentSplitDownstreamHodThresholds    .filterNot(_.severity == severityToRemove),
      httpTrafficThresholds                               = alertConfigBuilder.httpTrafficThresholds                              .map(threshold => if(severityToRemove == AlertSeverity.Warning) threshold.copy(warning = None) else if (severityToRemove == AlertSeverity.Critical) threshold.copy(critical = None) else threshold),
      httpStatusThresholds                                = alertConfigBuilder.httpStatusThresholds                               .filterNot(_.severity == severityToRemove),
      httpStatusPercentThresholds                         = alertConfigBuilder.httpStatusPercentThresholds                        .filterNot(_.severity == severityToRemove),
      metricsThresholds                                   = alertConfigBuilder.metricsThresholds                                  .map(threshold => if(severityToRemove == AlertSeverity.Warning) threshold.copy(warning = None) else if (severityToRemove == AlertSeverity.Critical) threshold.copy(critical = None) else threshold),
      logMessageThresholds                                = alertConfigBuilder.logMessageThresholds                               .filterNot(_.severity == severityToRemove)
    )

  def convertAlerts(alertConfigBuilder: AlertConfigBuilder, currentEnvironment: Environment): Alerts = {
    Alerts(
      averageCPUThreshold = convertAverageCPUThreshold(alertConfigBuilder.averageCPUThreshold, currentEnvironment),
      containerKillThreshold = convertContainerKillThreshold(alertConfigBuilder.containerKillThreshold, currentEnvironment),
      errorsLoggedThreshold = convertErrorsLoggedThreshold(alertConfigBuilder.errorsLoggedThreshold, currentEnvironment),
      exceptionThreshold = convertExceptionThreshold(alertConfigBuilder.exceptionThreshold, currentEnvironment),
      http5xxPercentThreshold = convertHttp5xxPercentThresholds(alertConfigBuilder.http5xxPercentThreshold, currentEnvironment),
      http5xxThreshold = convertHttp5xxThreshold(alertConfigBuilder.http5xxThreshold, currentEnvironment),
      httpAbsolutePercentSplitThreshold = convertHttpAbsolutePercentSplitThresholdAlert(alertConfigBuilder.httpAbsolutePercentSplitThresholds, currentEnvironment),
      httpStatusPercentThresholds = convertHttpStatusPercentThresholdAlerts(alertConfigBuilder.httpStatusPercentThresholds, currentEnvironment),
      httpStatusThresholds = convertHttpStatusThresholds(alertConfigBuilder.httpStatusThresholds, currentEnvironment),
      httpTrafficThresholds = convertHttpTrafficThresholds(alertConfigBuilder.httpTrafficThresholds, currentEnvironment),
      logMessageThresholds = convertLogMessageThresholdAlerts(alertConfigBuilder.logMessageThresholds, currentEnvironment),
      totalHttpRequestThreshold = convertTotalHttpRequestThreshold(alertConfigBuilder.totalHttpRequestThreshold, currentEnvironment),
      metricsThresholds = convertMetricsThreshold(alertConfigBuilder.metricsThresholds, currentEnvironment),
      http90PercentileResponseTimeThreshold = convertHttp90PercentileResponseTimeThreshold(alertConfigBuilder.http90PercentileResponseTimeThresholds, currentEnvironment)
    )
  }

  def convertAverageCPUThreshold(averageCPUThreshold: AverageCPUThreshold, currentEnvironment: Environment): Option[YamlAverageCPUThresholdAlert] = {
    Option.when(isGrafanaEnabled(averageCPUThreshold.alertingPlatform, currentEnvironment, AlertType.AverageCPUThreshold) && averageCPUThreshold.count < Int.MaxValue)(
      YamlAverageCPUThresholdAlert(averageCPUThreshold.count)
    )
  }

  def convertContainerKillThreshold(containerKillThreshold: ContainerKillThreshold, currentEnvironment: Environment): Option[YamlContainerKillThresholdAlert] = {
    Option.when(isGrafanaEnabled(containerKillThreshold.alertingPlatform, currentEnvironment, AlertType.ContainerKillThreshold) && containerKillThreshold.count < Int.MaxValue)(
      YamlContainerKillThresholdAlert(containerKillThreshold.count)
    )
  }

  def convertErrorsLoggedThreshold(errorsLoggedThreshold: ErrorsLoggedThreshold, currentEnvironment: Environment): Option[YamlErrorsLoggedThresholdAlert] = {
    Option.when(isGrafanaEnabled(errorsLoggedThreshold.alertingPlatform, currentEnvironment, AlertType.ErrorsLoggedThreshold) && errorsLoggedThreshold.count < Int.MaxValue)(
      YamlErrorsLoggedThresholdAlert(errorsLoggedThreshold.count)
    )
  }

  def convertExceptionThreshold(exceptionThreshold: ExceptionThreshold, currentEnvironment: Environment): Option[YamlExceptionThresholdAlert] = {
    Option.when(isGrafanaEnabled(exceptionThreshold.alertingPlatform, currentEnvironment, AlertType.ExceptionThreshold) && exceptionThreshold.count < Int.MaxValue)(
      YamlExceptionThresholdAlert(
        count = exceptionThreshold.count,
        severity = exceptionThreshold.severity.toString
      )
    )
  }

  def convertHttp5xxPercentThresholds(http5xxPercentThreshold: Http5xxPercentThreshold, currentEnvironment: Environment): Option[YamlHttp5xxPercentThresholdAlert] = {
    Option.when(isGrafanaEnabled(http5xxPercentThreshold.alertingPlatform, currentEnvironment, AlertType.Http5xxPercentThreshold) && http5xxPercentThreshold.percentage <= 100.0)(
      YamlHttp5xxPercentThresholdAlert(
        percentage = http5xxPercentThreshold.percentage,
        minimumHttp5xxCountThreshold = http5xxPercentThreshold.minimumHttp5xxCountThreshold,
        severity = http5xxPercentThreshold.severity.toString
      )
    )
  }

  def convertHttp5xxThreshold(http5xxThreshold: Http5xxThreshold, currentEnvironment: Environment): Option[YamlHttp5xxThresholdAlert] = {
    Option.when(isGrafanaEnabled(http5xxThreshold.alertingPlatform, currentEnvironment, AlertType.Http5xxThreshold) && http5xxThreshold.count < Int.MaxValue)(
      YamlHttp5xxThresholdAlert(
        count = http5xxThreshold.count,
        severity = http5xxThreshold.severity.toString
      )
    )
  }
  def convertHttpAbsolutePercentSplitThresholdAlert(httpAbsolutePercentSplitThresholds: Seq[HttpAbsolutePercentSplitThreshold], currentEnvironment: Environment): Option[Seq[YamlHttpAbsolutePercentSplitThresholdAlert]] = {
    val converted =  httpAbsolutePercentSplitThresholds.withFilter(alert => isGrafanaEnabled(alert.alertingPlatform, currentEnvironment, AlertType.HttpAbsolutePercentSplitThreshold) && alert.absoluteThreshold < Int.MaxValue).map {
      threshold =>
        YamlHttpAbsolutePercentSplitThresholdAlert(
          percentThreshold  = threshold.percentThreshold,
          crossover         = threshold.crossOver,
          absoluteThreshold = threshold.absoluteThreshold,
          hysteresis        = threshold.hysteresis,
          excludeSpikes     = threshold.excludeSpikes,
          errorFilter       = threshold.errorFilter,
          severity          = threshold.severity.toString
        )
    }
    Option.when(converted.nonEmpty)(converted)
  }

  def convertHttpStatusThresholds(httpStatusThresholds: Seq[HttpStatusThreshold], currentEnvironment: Environment): Option[Seq[YamlHttpStatusThresholdAlert]] = {
    val converted = httpStatusThresholds.withFilter(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.HttpStatusThreshold)).map { threshold =>
      YamlHttpStatusThresholdAlert(
        count = threshold.count,
        httpMethod = threshold.httpMethod.toString,
        httpStatus = threshold.httpStatus.status,
        severity = threshold.severity.toString
      )
    }
    Option.when(converted.nonEmpty)(converted)
  }

  def convertLogMessageThresholdAlerts(logMessageThresholds: Seq[LogMessageThreshold], currentEnvironment: Environment): Option[Seq[YamlLogMessageThresholdAlert]] = {
    val converted = logMessageThresholds.withFilter(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.LogMessageThreshold)).map { threshold =>
      YamlLogMessageThresholdAlert(
        message = threshold.message,
        count = threshold.count,
        lessThanMode = threshold.lessThanMode,
        severity = threshold.severity.toString
      )
    }
    Option.when(converted.nonEmpty)(converted)
  }

  def convertHttpStatusPercentThresholdAlerts(httpStatusPercentThresholds: Seq[HttpStatusPercentThreshold], currentEnvironment: Environment): Option[Seq[YamlHttpStatusPercentThresholdAlert]] = {
    val converted = httpStatusPercentThresholds.withFilter(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.HttpStatusPercentThreshold)).map { threshold =>
      YamlHttpStatusPercentThresholdAlert(
        percentage = threshold.percentage,
        httpMethod = threshold.httpMethod.toString,
        httpStatus = threshold.httpStatus.status,
        severity = threshold.severity.toString
      )
    }
    Option.when(converted.nonEmpty)(converted)
  }

  def convertHttpTrafficThresholds(httpTrafficThresholds: Seq[HttpTrafficThreshold], currentEnvironment: Environment): Option[Seq[YamlHttpTrafficThresholdAlert]] = {
    val converted = httpTrafficThresholds.flatMap { threshold =>
      if (isGrafanaEnabled(threshold.alertingPlatform, currentEnvironment, AlertType.HttpTrafficThreshold)) {
        Seq(
          threshold.warning.map { warningCount =>
            YamlHttpTrafficThresholdAlert(
              count = warningCount,
              maxMinutesBelowThreshold = threshold.maxMinutesBelowThreshold,
              severity = "warning"
            )
          },
          threshold.critical.map { criticalCount =>
            YamlHttpTrafficThresholdAlert(
              count = criticalCount,
              maxMinutesBelowThreshold = threshold.maxMinutesBelowThreshold,
              severity = "critical"
            )
          }
        ).flatten
      } else {
        Seq.empty
      }
    }
    Option.when(converted.nonEmpty)(converted)
  }

  def convertTotalHttpRequestThreshold(totalHttpRequestThreshold: TotalHttpRequestThreshold, currentEnvironment: Environment): Option[YamlTotalHttpRequestThresholdAlert] = {
    Option.when(isGrafanaEnabled(totalHttpRequestThreshold.alertingPlatform, currentEnvironment, AlertType.TotalHttpRequestThreshold) && totalHttpRequestThreshold.count < Int.MaxValue)(
      YamlTotalHttpRequestThresholdAlert(totalHttpRequestThreshold.count)
    )
  }

  def convertMetricsThreshold(metricsThreshold: Seq[MetricsThreshold], currentEnvironment: Environment): Option[Seq[YamlMetricsThresholdAlert]] = {
    val converted = metricsThreshold.flatMap { threshold =>
      if (isGrafanaEnabled(threshold.alertingPlatform, currentEnvironment, AlertType.MetricsThreshold)) {
        Seq(
          threshold.warning.map { warningCount =>
            YamlMetricsThresholdAlert(
              count = warningCount,
              name = threshold.name,
              query = threshold.query,
              severity = "warning",
              invert = threshold.invert
            )
          },
          threshold.critical.map { criticalCount =>
            YamlMetricsThresholdAlert(
              count = criticalCount,
              name = threshold.name,
              query = threshold.query,
              severity = "critical",
              invert = threshold.invert
            )
          }
        ).flatten
      } else {
        Seq.empty
      }
    }
    Option.when(converted.nonEmpty)(converted)
  }


  def convertHttp90PercentileResponseTimeThreshold(http90PercentileResponseTimeThreshold: Seq[Http90PercentileResponseTimeThreshold], currentEnvironment: Environment): Option[Seq[YamlHttp90PercentileResponseTimeThresholdAlert]] = {
    val converted = http90PercentileResponseTimeThreshold.flatMap { threshold =>
      if (isGrafanaEnabled(threshold.alertingPlatform, currentEnvironment, AlertType.Http90PercentileResponseTimeThreshold)) {
        Seq(
          threshold.warning.map { warningCount =>
            YamlHttp90PercentileResponseTimeThresholdAlert(
              count = warningCount,
              timePeriod = threshold.timePeriod,
              severity = "warning"
            )
          },
          threshold.critical.map { criticalCount =>
            YamlHttp90PercentileResponseTimeThresholdAlert(
              count = criticalCount,
              timePeriod = threshold.timePeriod,
              severity = "critical"
            )
          }
        ).flatten
      } else {
        Seq.empty
      }
    }
    Option.when(converted.nonEmpty)(converted)
  }

}
