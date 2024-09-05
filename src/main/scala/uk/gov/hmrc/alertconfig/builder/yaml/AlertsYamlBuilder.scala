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

import uk.gov.hmrc.alertconfig.builder._
import uk.gov.hmrc.alertconfig.builder.yaml.YamlWriter.mapper

import java.io.File

object AlertsYamlBuilder {
  import AppConfigValidator._
  val logger = new Logger()

  def run(alertConfigs: Seq[AlertConfig], environment: String): Unit = {
    val currentEnvironment = Environment.get(environment)
    val topLevelConfig     = TopLevelConfig(convert(alertConfigs, currentEnvironment))
    logger.debug(s"Generating alert config YAML for $currentEnvironment")

    mapper.writeValue(new File(s"./target/output/services.yml"), topLevelConfig)
    logger.debug(s"Done generating alert config YAML for $currentEnvironment")
  }

  def convert(alertConfigs: Seq[AlertConfig], currentEnvironment: Environment): Seq[ServiceConfig] = {
    alertConfigs.flatMap(convert(_, currentEnvironment))
  }

  def convert(alertConfig: AlertConfig, currentEnvironment: Environment): Seq[ServiceConfig] = {
    val filtered                 = alertConfig.environmentConfig.filter(_.enabledEnvironments.contains(currentEnvironment))
    val enabledIntegrationsInEnv = filtered.map(_.integrationName).toSet
    val integrationSeveritiesForEnv = filtered
      .map(builder => builder.integrationName -> builder.enabledEnvironments.getOrElse(currentEnvironment, Set()))
      .toMap

    alertConfig.alertConfig.flatMap(convert(_, enabledIntegrationsInEnv, integrationSeveritiesForEnv))
  }

  def convert(alertConfigBuilder: AlertConfigBuilder,
              environmentDefinedIntegrations: Set[String],
              integrationSeveritiesForEnv: Map[String, Set[Severity]]): Option[ServiceConfig] = {
    val enabledIntegrations = alertConfigBuilder.integrations.toSet.intersect(environmentDefinedIntegrations)
    if (enabledIntegrations.isEmpty || !serviceDeployedInEnv(alertConfigBuilder.serviceName, alertConfigBuilder.platformService)) {
      None
    } else {
      val finalAlertConfigBuilder = removeUnusedAlerts(alertConfigBuilder, integrationSeveritiesForEnv)
      Some(
        ServiceConfig(
          service = finalAlertConfigBuilder.serviceName.trim.toLowerCase.replaceAll(" ", "-"),
          alerts = convertAlerts(finalAlertConfigBuilder),
          pagerduty = enabledIntegrations.map(integration => PagerDuty(integrationKeyName = integration)).toSeq
        ))
    }
  }

  private def removeUnusedAlerts(alertConfigBuilder: AlertConfigBuilder,
                                 integrationSeveritiesForEnv: Map[String, Set[Severity]]): AlertConfigBuilder = {
    val uniqueEnabledSeveritiesForServiceInEnv =
      integrationSeveritiesForEnv.view.filterKeys(alertConfigBuilder.integrations.contains).values.flatten.toSet
    if (uniqueEnabledSeveritiesForServiceInEnv.contains(Severity.Critical) && !uniqueEnabledSeveritiesForServiceInEnv.contains(Severity.Warning)) {
      removeUnusedAlerts(alertConfigBuilder, AlertSeverity.Warning)
    } else if (uniqueEnabledSeveritiesForServiceInEnv.contains(Severity.Warning) && !uniqueEnabledSeveritiesForServiceInEnv.contains(
        Severity.Critical)) {
      removeUnusedAlerts(alertConfigBuilder, AlertSeverity.Critical)
    } else {
      alertConfigBuilder
    }
  }

  private def removeUnusedAlerts(alertConfigBuilder: AlertConfigBuilder, severityToRemove: AlertSeverity): AlertConfigBuilder =
    alertConfigBuilder.copy(
      exceptionThreshold =
        if (alertConfigBuilder.exceptionThreshold.map(_.severity).contains(severityToRemove)) None
        else alertConfigBuilder.exceptionThreshold,
      http5xxThreshold =
        if (alertConfigBuilder.http5xxThreshold.map(_.severity).contains(severityToRemove)) None
        else alertConfigBuilder.http5xxThreshold,
      http5xxPercentThreshold =
        if (alertConfigBuilder.http5xxPercentThreshold.map(_.severity).contains(severityToRemove)) None
        else alertConfigBuilder.http5xxPercentThreshold,
      http90PercentileResponseTimeThresholds = alertConfigBuilder.http90PercentileResponseTimeThresholds.map(threshold =>
        if (severityToRemove == AlertSeverity.Warning) threshold.copy(warning = None)
        else if (severityToRemove == AlertSeverity.Critical) threshold.copy(critical = None)
        else threshold),
      httpAbsolutePercentSplitThresholds = alertConfigBuilder.httpAbsolutePercentSplitThresholds.filterNot(_.severity == severityToRemove),
      httpAbsolutePercentSplitDownstreamServiceThresholds =
        alertConfigBuilder.httpAbsolutePercentSplitDownstreamServiceThresholds.filterNot(_.severity == severityToRemove),
      httpAbsolutePercentSplitDownstreamHodThresholds =
        alertConfigBuilder.httpAbsolutePercentSplitDownstreamHodThresholds.filterNot(_.severity == severityToRemove),
      httpTrafficThresholds = alertConfigBuilder.httpTrafficThresholds.map(threshold =>
        if (severityToRemove == AlertSeverity.Warning) threshold.copy(warning = None)
        else if (severityToRemove == AlertSeverity.Critical) threshold.copy(critical = None)
        else threshold),
      httpStatusThresholds = alertConfigBuilder.httpStatusThresholds.filterNot(_.severity == severityToRemove),
      httpStatusPercentThresholds = alertConfigBuilder.httpStatusPercentThresholds.filterNot(_.severity == severityToRemove),
      metricsThresholds = alertConfigBuilder.metricsThresholds.map(threshold =>
        if (severityToRemove == AlertSeverity.Warning) threshold.copy(warning = None)
        else if (severityToRemove == AlertSeverity.Critical) threshold.copy(critical = None)
        else threshold),
      logMessageThresholds = alertConfigBuilder.logMessageThresholds.filterNot(_.severity == severityToRemove)
    )

  def convertAlerts(alertConfigBuilder: AlertConfigBuilder): Alerts = {
    Alerts(
      averageCPUThreshold = convertAverageCPUThreshold(alertConfigBuilder.averageCPUThreshold),
      containerKillThreshold = convertContainerKillThreshold(alertConfigBuilder.containerKillThreshold),
      errorsLoggedThreshold = convertErrorsLoggedThreshold(alertConfigBuilder.errorsLoggedThreshold),
      exceptionThreshold = convertExceptionThreshold(alertConfigBuilder.exceptionThreshold),
      http5xxPercentThreshold = convertHttp5xxPercentThresholds(alertConfigBuilder.http5xxPercentThreshold),
      http5xxThreshold = convertHttp5xxThreshold(alertConfigBuilder.http5xxThreshold),
      httpAbsolutePercentSplitThreshold = convertHttpAbsolutePercentSplitThresholdAlert(alertConfigBuilder.httpAbsolutePercentSplitThresholds),
      httpAbsolutePercentSplitDownstreamHodThreshold =
        convertHttpAbsolutePercentSplitDownstreamHodThresholdAlert(alertConfigBuilder.httpAbsolutePercentSplitDownstreamHodThresholds),
      httpAbsolutePercentSplitDownstreamServiceThreshold =
        convertHttpAbsolutePercentSplitDownstreamServiceThresholdAlert(alertConfigBuilder.httpAbsolutePercentSplitDownstreamServiceThresholds),
      httpStatusPercentThresholds = convertHttpStatusPercentThresholdAlerts(alertConfigBuilder.httpStatusPercentThresholds),
      httpStatusThresholds = convertHttpStatusThresholds(alertConfigBuilder.httpStatusThresholds),
      httpTrafficThresholds = convertHttpTrafficThresholds(alertConfigBuilder.httpTrafficThresholds),
      logMessageThresholds = convertLogMessageThresholdAlerts(alertConfigBuilder.logMessageThresholds),
      totalHttpRequestThreshold = convertTotalHttpRequestThreshold(alertConfigBuilder.totalHttpRequestThreshold),
      metricsThresholds = convertMetricsThreshold(alertConfigBuilder.metricsThresholds),
      http90PercentileResponseTimeThreshold = convertHttp90PercentileResponseTimeThreshold(alertConfigBuilder.http90PercentileResponseTimeThresholds)
    )
  }

  private def convertAverageCPUThreshold(averageCPUThreshold: Option[AverageCPUThreshold]): Option[YamlAverageCPUThresholdAlert] = {
    averageCPUThreshold.map(threshold => YamlAverageCPUThresholdAlert(threshold.count))
  }

  private def convertContainerKillThreshold(containerKillThreshold: Option[ContainerKillThreshold]): Option[YamlContainerKillThresholdAlert] = {
    containerKillThreshold.map(threshold => YamlContainerKillThresholdAlert(threshold.count))
  }

  private def convertErrorsLoggedThreshold(errorsLoggedThreshold: Option[ErrorsLoggedThreshold]): Option[YamlErrorsLoggedThresholdAlert] = {
    errorsLoggedThreshold.map(threshold => YamlErrorsLoggedThresholdAlert(threshold.count))
  }

  private def convertExceptionThreshold(exceptionThreshold: Option[ExceptionThreshold]): Option[YamlExceptionThresholdAlert] = {
    exceptionThreshold.map(threshold => YamlExceptionThresholdAlert(
      count = threshold.count,
      severity = threshold.severity.toString
    ))
  }

  private def convertHttp5xxPercentThresholds(http5xxPercentThreshold: Option[Http5xxPercentThreshold]): Option[YamlHttp5xxPercentThresholdAlert] = {
    http5xxPercentThreshold.map(threshold => YamlHttp5xxPercentThresholdAlert(
      percentage = threshold.percentage,
      minimumHttp5xxCountThreshold = threshold.minimumHttp5xxCountThreshold,
      severity = threshold.severity.toString
    ))
  }

  private def convertHttp5xxThreshold(http5xxThreshold: Option[Http5xxThreshold]): Option[YamlHttp5xxThresholdAlert] = {
    http5xxThreshold.map(threshold => YamlHttp5xxThresholdAlert(
      count = threshold.count,
      severity = threshold.severity.toString
    ))
  }

  private def convertHttpAbsolutePercentSplitThresholdAlert(
      httpAbsolutePercentSplitThresholds: Seq[HttpAbsolutePercentSplitThreshold]): Option[Seq[YamlHttpAbsolutePercentSplitThresholdAlert]] = {
    val converted = httpAbsolutePercentSplitThresholds
      .map { threshold =>
        YamlHttpAbsolutePercentSplitThresholdAlert(
          percentThreshold = threshold.percentThreshold,
          crossover = threshold.crossOver,
          absoluteThreshold = threshold.absoluteThreshold,
          hysteresis = threshold.hysteresis,
          excludeSpikes = threshold.excludeSpikes,
          errorFilter = threshold.errorFilter,
          severity = threshold.severity.toString
        )
      }
    Option.when(converted.nonEmpty)(converted)
  }

  private def convertHttpAbsolutePercentSplitDownstreamHodThresholdAlert(
      httpAbsolutePercentSplitDownstreamHodThresholds: Seq[HttpAbsolutePercentSplitDownstreamHodThreshold])
      : Option[Seq[YamlHttpAbsolutePercentSplitDownstreamHodThresholdAlert]] = {
    val converted = httpAbsolutePercentSplitDownstreamHodThresholds
      .map { threshold =>
        YamlHttpAbsolutePercentSplitDownstreamHodThresholdAlert(
          percentThreshold = threshold.percentThreshold,
          crossover = threshold.crossOver,
          absoluteThreshold = threshold.absoluteThreshold,
          hysteresis = threshold.hysteresis,
          excludeSpikes = threshold.excludeSpikes,
          errorFilter = threshold.errorFilter,
          target = threshold.target,
          severity = threshold.severity.toString
        )
      }
    Option.when(converted.nonEmpty)(converted)
  }

  private def convertHttpAbsolutePercentSplitDownstreamServiceThresholdAlert(
      httpAbsolutePercentSplitDownstreamServiceThresholds: Seq[HttpAbsolutePercentSplitDownstreamServiceThreshold])
      : Option[Seq[YamlHttpAbsolutePercentSplitDownstreamServiceThresholdAlert]] = {
    val converted = httpAbsolutePercentSplitDownstreamServiceThresholds
      .map { threshold =>
        YamlHttpAbsolutePercentSplitDownstreamServiceThresholdAlert(
          percentThreshold = threshold.percentThreshold,
          crossover = threshold.crossOver,
          absoluteThreshold = threshold.absoluteThreshold,
          hysteresis = threshold.hysteresis,
          excludeSpikes = threshold.excludeSpikes,
          errorFilter = threshold.errorFilter,
          target = threshold.target,
          severity = threshold.severity.toString
        )
      }
    Option.when(converted.nonEmpty)(converted)
  }

  private def convertHttpStatusThresholds(httpStatusThresholds: Seq[HttpStatusThreshold]): Option[Seq[YamlHttpStatusThresholdAlert]] = {
    val converted =
      httpStatusThresholds.map { threshold =>
        YamlHttpStatusThresholdAlert(
          count = threshold.count,
          httpMethod = threshold.httpMethod.toString,
          httpStatus = threshold.httpStatus.status,
          severity = threshold.severity.toString
        )
      }
    Option.when(converted.nonEmpty)(converted)
  }

  private def convertLogMessageThresholdAlerts(logMessageThresholds: Seq[LogMessageThreshold]): Option[Seq[YamlLogMessageThresholdAlert]] = {
    val converted =
      logMessageThresholds.map { threshold =>
        YamlLogMessageThresholdAlert(
          message = threshold.message,
          count = threshold.count,
          lessThanMode = threshold.lessThanMode,
          severity = threshold.severity.toString
        )
      }
    Option.when(converted.nonEmpty)(converted)
  }

  private def convertHttpStatusPercentThresholdAlerts(
      httpStatusPercentThresholds: Seq[HttpStatusPercentThreshold]): Option[Seq[YamlHttpStatusPercentThresholdAlert]] = {
    val converted = httpStatusPercentThresholds
      .map { threshold =>
        YamlHttpStatusPercentThresholdAlert(
          percentage = threshold.percentage,
          httpMethod = threshold.httpMethod.toString,
          httpStatus = threshold.httpStatus.status,
          severity = threshold.severity.toString
        )
      }
    Option.when(converted.nonEmpty)(converted)
  }

  private def convertHttpTrafficThresholds(httpTrafficThresholds: Seq[HttpTrafficThreshold]): Option[Seq[YamlHttpTrafficThresholdAlert]] = {
    val converted = httpTrafficThresholds.flatMap { threshold =>
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
    }
    Option.when(converted.nonEmpty)(converted)
  }

  private def convertTotalHttpRequestThreshold(totalHttpRequestThreshold: Option[TotalHttpRequestThreshold]): Option[YamlTotalHttpRequestThresholdAlert] = {
    totalHttpRequestThreshold.map(threshold =>
      YamlTotalHttpRequestThresholdAlert(threshold.count))
  }

  private def convertMetricsThreshold(metricsThreshold: Seq[MetricsThreshold]): Option[Seq[YamlMetricsThresholdAlert]] = {
    val converted = metricsThreshold.flatMap { threshold =>
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
    }
    Option.when(converted.nonEmpty)(converted)
  }

  private def convertHttp90PercentileResponseTimeThreshold(http90PercentileResponseTimeThreshold: Seq[Http90PercentileResponseTimeThreshold])
      : Option[Seq[YamlHttp90PercentileResponseTimeThresholdAlert]] = {
    val converted = http90PercentileResponseTimeThreshold.flatMap { threshold =>
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
    }
    Option.when(converted.nonEmpty)(converted)
  }

}
