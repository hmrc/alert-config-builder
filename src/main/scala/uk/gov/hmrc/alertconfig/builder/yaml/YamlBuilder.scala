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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import uk.gov.hmrc.alertconfig.builder.AlertingPlatform.Grafana
import uk.gov.hmrc.alertconfig.builder.GrafanaMigration.isGrafanaEnabled
import uk.gov.hmrc.alertconfig.builder.{AlertConfig, AlertConfigBuilder, AlertType, AlertingPlatform, AverageCPUThreshold, ContainerKillThreshold, Environment, ErrorsLoggedThreshold, ExceptionThreshold, Http5xxPercentThreshold, Http5xxThreshold, HttpStatusPercentThreshold, HttpStatusThreshold, HttpTrafficThreshold, LogMessageThreshold, Logger, MetricsThreshold, TotalHttpRequestThreshold}

import java.io.File

object YamlBuilder {

  val logger = new Logger()

  def run(alertConfigs: Seq[AlertConfig], environment: String): Unit = {

    val currentEnvironment = Environment.get(environment)
    val topLevelConfig     = TopLevelConfig(convert(alertConfigs, currentEnvironment))
    logger.debug(s"Generating YAML for $currentEnvironment")

    val mapper = new ObjectMapper(
      new YAMLFactory()
        .disable(Feature.WRITE_DOC_START_MARKER)
        .enable(Feature.INDENT_ARRAYS_WITH_INDICATOR)
    )
    mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
    mapper.registerModule(DefaultScalaModule)

    mapper.writeValue(new File(s"./target/output/services.yml"), topLevelConfig)
    logger.debug(s"Done generating YAML for $currentEnvironment")
  }

  def convert(alertConfigs: Seq[AlertConfig], currentEnvironment: Environment): Seq[ServiceConfig] = {
    alertConfigs.flatMap(convert(_, currentEnvironment))
  }

  def convert(alertConfig: AlertConfig, currentEnvironment: Environment): Seq[ServiceConfig] = {
    val filtered             = alertConfig.environmentConfig.filter(_.enabledEnvironments.contains(currentEnvironment))
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
      containerKillThreshold = convertContainerKillThreshold(alertConfigBuilder.containerKillThreshold),
      errorsLoggedThreshold = convertErrorsLoggedThreshold(alertConfigBuilder.errorsLoggedThreshold),
      exceptionThreshold = convertExceptionThreshold(alertConfigBuilder.exceptionThreshold),
      http5xxPercentThreshold = convertHttp5xxPercentThresholds(alertConfigBuilder.http5xxPercentThreshold),
      http5xxThreshold = convertHttp5xxThreshold(alertConfigBuilder.http5xxThreshold),
      httpStatusPercentThresholds = convertHttpStatusPercentThresholdAlerts(alertConfigBuilder.httpStatusPercentThresholds),
      httpStatusThresholds = convertHttpStatusThresholds(alertConfigBuilder.httpStatusThresholds),
      httpTrafficThresholds = convertHttpTrafficThresholds(alertConfigBuilder.httpTrafficThresholds),
      logMessageThresholds = convertLogMessageThresholdAlerts(alertConfigBuilder.logMessageThresholds),
      totalHttpRequestThreshold = convertTotalHttpRequestThreshold(alertConfigBuilder.totalHttpRequestThreshold),
      metricsThresholds = convertMetricsThreshold(alertConfigBuilder.metricsThresholds)
    )
  }

  def convertAverageCPUThreshold(averageCPUThreshold: AverageCPUThreshold, currentEnvironment: Environment): Option[YamlAverageCPUThresholdAlert] = {
    Option.when(isGrafanaEnabled(averageCPUThreshold.alertingPlatform, currentEnvironment, AlertType.AverageCPUThreshold) && averageCPUThreshold.count < Int.MaxValue)(
      YamlAverageCPUThresholdAlert(averageCPUThreshold.count)
    )
  }

  def convertContainerKillThreshold(containerKillThreshold: ContainerKillThreshold): Option[YamlContainerKillThresholdAlert] = {
    Option.when(containerKillThreshold.alertingPlatform == AlertingPlatform.Grafana)(
      YamlContainerKillThresholdAlert(containerKillThreshold.count)
    )
  }

  def convertErrorsLoggedThreshold(errorsLoggedThreshold: ErrorsLoggedThreshold): Option[YamlErrorsLoggedThresholdAlert] = {
    Option.when(errorsLoggedThreshold.alertingPlatform == AlertingPlatform.Grafana)(
      YamlErrorsLoggedThresholdAlert(errorsLoggedThreshold.count)
    )
  }

  def convertExceptionThreshold(exceptionThreshold: ExceptionThreshold): Option[YamlExceptionThresholdAlert] = {
    Option.when(exceptionThreshold.alertingPlatform == AlertingPlatform.Grafana)(
      YamlExceptionThresholdAlert(
        count = exceptionThreshold.count,
        severity = exceptionThreshold.severity.toString
      )
    )
  }

  def convertHttp5xxPercentThresholds(http5xxPercentThreshold: Http5xxPercentThreshold): Option[YamlHttp5xxPercentThresholdAlert] = {
    Option.when(http5xxPercentThreshold.alertingPlatform == AlertingPlatform.Grafana)(
      YamlHttp5xxPercentThresholdAlert(
        percentage = http5xxPercentThreshold.percentage,
        severity = http5xxPercentThreshold.severity.toString
      )
    )
  }

  def convertHttp5xxThreshold(http5xxThreshold: Http5xxThreshold): Option[YamlHttp5xxThresholdAlert] = {
    Option.when(http5xxThreshold.alertingPlatform == AlertingPlatform.Grafana)(
      YamlHttp5xxThresholdAlert(
        count = http5xxThreshold.count,
        severity = http5xxThreshold.severity.toString
      )
    )
  }

  def convertHttpStatusThresholds(httpStatusThresholds: Seq[HttpStatusThreshold]): Option[Seq[YamlHttpStatusThresholdAlert]] = {
    val converted = httpStatusThresholds.withFilter(_.alertingPlatform == Grafana).map { threshold =>
      YamlHttpStatusThresholdAlert(
        count = threshold.count,
        httpMethod = threshold.httpMethod.toString,
        httpStatus = threshold.httpStatus.status,
        severity = threshold.severity.toString
      )
    }
    Option.when(converted.nonEmpty)(converted)
  }

  def convertLogMessageThresholdAlerts(logMessageThresholds: Seq[LogMessageThreshold]): Option[Seq[YamlLogMessageThresholdAlert]] = {
    val converted = logMessageThresholds.withFilter(_.alertingPlatform == Grafana).map { threshold =>
      YamlLogMessageThresholdAlert(
        message = threshold.message,
        count = threshold.count,
        lessThanMode = threshold.lessThanMode,
        severity = threshold.severity.toString
      )
    }
    Option.when(converted.nonEmpty)(converted)
  }

  def convertHttpStatusPercentThresholdAlerts(
                                               httpStatusPercentThresholds: Seq[HttpStatusPercentThreshold]): Option[Seq[YamlHttpStatusPercentThresholdAlert]] = {
    val converted = httpStatusPercentThresholds.withFilter(_.alertingPlatform == Grafana).map { threshold =>
      YamlHttpStatusPercentThresholdAlert(
        percentage = threshold.percentage,
        httpMethod = threshold.httpMethod.toString,
        httpStatus = threshold.httpStatus.status,
        severity = threshold.severity.toString
      )
    }
    Option.when(converted.nonEmpty)(converted)
  }

  def convertHttpTrafficThresholds(httpTrafficThresholds: Seq[HttpTrafficThreshold]): Option[Seq[YamlHttpTrafficThresholdAlert]] = {
    val converted = httpTrafficThresholds.flatMap { threshold =>
      if (threshold.alertingPlatform == Grafana) {
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

  def convertTotalHttpRequestThreshold(totalHttpRequestThreshold: TotalHttpRequestThreshold): Option[YamlTotalHttpRequestThresholdAlert] = {
    Option.when(totalHttpRequestThreshold.alertingPlatform == AlertingPlatform.Grafana)(
      YamlTotalHttpRequestThresholdAlert(totalHttpRequestThreshold.count)
    )
  }

  def convertMetricsThreshold(metricsThreshold: Seq[MetricsThreshold]): Option[Seq[YamlMetricsThresholdAlert]] = {
    val converted = metricsThreshold.flatMap { threshold =>
      if (threshold.alertingPlatform == Grafana) {
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
