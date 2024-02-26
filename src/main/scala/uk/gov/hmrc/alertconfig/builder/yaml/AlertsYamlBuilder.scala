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
    val enabledHandlersInEnv = filtered.map(_.handlerName).toSet
    alertConfig.alertConfig.flatMap(convert(_, enabledHandlersInEnv, currentEnvironment))
  }

  def convert(alertConfigBuilder: AlertConfigBuilder, environmentDefinedHandlers: Set[String], currentEnvironment: Environment): Option[ServiceConfig] = {
    val enabledHandlers = alertConfigBuilder.handlers.toSet.intersect(environmentDefinedHandlers)
    if (enabledHandlers.isEmpty) {
      None
    } else {
      Some(
        ServiceConfig(
          service = alertConfigBuilder.serviceName.trim.toLowerCase.replaceAll(" ", "-"),
          alerts = convertAlerts(alertConfigBuilder, currentEnvironment),
          pagerduty = enabledHandlers.map(handler => PagerDuty(integrationKeyName = handler)).toSeq
        ))
    }
  }

  def convertAlerts(alertConfigBuilder: AlertConfigBuilder, currentEnvironment: Environment): Alerts = {
    Alerts(
      averageCPUThreshold = convertAverageCPUThreshold(alertConfigBuilder.averageCPUThreshold, currentEnvironment),
      containerKillThreshold = convertContainerKillThreshold(alertConfigBuilder.containerKillThreshold, currentEnvironment),
      errorsLoggedThreshold = convertErrorsLoggedThreshold(alertConfigBuilder.errorsLoggedThreshold, currentEnvironment),
      exceptionThreshold = convertExceptionThreshold(alertConfigBuilder.exceptionThreshold, currentEnvironment),
      http5xxPercentThreshold = convertHttp5xxPercentThresholds(alertConfigBuilder.http5xxPercentThreshold, currentEnvironment),
      http5xxThreshold = convertHttp5xxThreshold(alertConfigBuilder.http5xxThreshold, currentEnvironment),
      httpStatusPercentThresholds = convertHttpStatusPercentThresholdAlerts(alertConfigBuilder.httpStatusPercentThresholds, currentEnvironment),
      httpStatusThresholds = convertHttpStatusThresholds(alertConfigBuilder.httpStatusThresholds, currentEnvironment),
      httpTrafficThresholds = convertHttpTrafficThresholds(alertConfigBuilder.httpTrafficThresholds, currentEnvironment),
      logMessageThresholds = convertLogMessageThresholdAlerts(alertConfigBuilder.logMessageThresholds, currentEnvironment),
      totalHttpRequestThreshold = convertTotalHttpRequestThreshold(alertConfigBuilder.totalHttpRequestThreshold, currentEnvironment),
      metricsThresholds = convertMetricsThreshold(alertConfigBuilder.metricsThresholds, currentEnvironment)
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

}
