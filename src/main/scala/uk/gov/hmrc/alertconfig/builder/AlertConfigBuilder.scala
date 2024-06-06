/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.alertconfig.builder

import org.yaml.snakeyaml.Yaml
import uk.gov.hmrc.alertconfig.builder.GrafanaMigration.isGrafanaEnabled

import java.io.{File, FileInputStream, FileNotFoundException}
import scala.jdk.CollectionConverters._

trait Builder[T] {
  def build: T
}

case class AlertConfigBuilder(
    serviceName: String,
    integrations: Seq[String] = Seq("noop"),
    errorsLoggedThreshold: ErrorsLoggedThreshold = ErrorsLoggedThreshold(),
    exceptionThreshold: ExceptionThreshold = ExceptionThreshold(),
    http5xxThreshold: Http5xxThreshold = Http5xxThreshold(),
    http5xxPercentThreshold: Http5xxPercentThreshold = Http5xxPercentThreshold(100.0),
    http90PercentileResponseTimeThresholds: Seq[Http90PercentileResponseTimeThreshold] = Nil,
    httpAbsolutePercentSplitThresholds: Seq[HttpAbsolutePercentSplitThreshold] = Nil,
    httpAbsolutePercentSplitDownstreamServiceThresholds: Seq[HttpAbsolutePercentSplitDownstreamServiceThreshold] = Nil,
    httpAbsolutePercentSplitDownstreamHodThresholds: Seq[HttpAbsolutePercentSplitDownstreamHodThreshold] = Nil,
    containerKillThreshold: ContainerKillThreshold = ContainerKillThreshold(1),
    httpTrafficThresholds: Seq[HttpTrafficThreshold] = Nil,
    httpStatusThresholds: Seq[HttpStatusThreshold] = Nil,
    httpStatusPercentThresholds: Seq[HttpStatusPercentThreshold] = Nil,
    metricsThresholds: Seq[MetricsThreshold] = Nil,
    logMessageThresholds: Seq[LogMessageThreshold] = Nil,
    totalHttpRequestThreshold: TotalHttpRequestThreshold = TotalHttpRequestThreshold(),
    averageCPUThreshold: AverageCPUThreshold = AverageCPUThreshold(),
    platformService: Boolean = false
) extends Builder[Option[String]] {

  import spray.json._

  val logger = new Logger()

  /** Sets integrations for the configuration, e.g. .withIntegrations("dass-cyclone")
    * @param string
    *   The name of your pagerduty integration. This integration must already exist in PagerDuty; alert-config will not create it for you.
    */
  def withIntegrations(integrations: String*) =
    this.copy(integrations = integrations)

  /** This alert will notify when your microservice logs a specified number of logs at ERROR log level within a 15-minute window.
    *
    * @param errorsLoggedThreshold
    *   The number of logs at ERROR level that this alert will trigger on
    * @param alertingPlatform
    *   The that platform this alert should go to (Sensu or Grafana)
    * @return
    * @example
    *   <pre> {@code .withErrorsLoggedThreshold(5) # alert when 5 logs at ERROR level are logged in a 15-minute window } </pre>
    */
  def withErrorsLoggedThreshold(errorsLoggedThreshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(errorsLoggedThreshold = ErrorsLoggedThreshold(errorsLoggedThreshold, alertingPlatform))

  /** This alert will notify when your microservice throws a specified number of exceptions, at log level ERROR, within a 15-minute window.
    * @param count
    *   The number of exceptions thrown that this alert will trigger on
    * @param alertingPlatform
    *   The that platform this alert should go to (Sensu or Grafana)
    * @return
    * @example
    *   <pre> {@code .withExceptionThreshold(count: Int, severity: AlertSeverity = AlertSeverity.Critical) } </pre>
    */
  def withExceptionThreshold(exceptionThreshold: Int,
                             severity: AlertSeverity = AlertSeverity.Critical,
                             alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(exceptionThreshold = ExceptionThreshold(exceptionThreshold, severity, alertingPlatform = alertingPlatform))

  /** This alert will notify when the number of http responses returning a 5xx http status code exceeds a given threshold within a 15-minute window.
    *
    * @param http5xxThreshold
    *   The number of all http responses with a 5xx status code to alert on
    * @param severity
    *   Whether to raise the alert as critical or warning
    * @param alertingPlatform
    *   The that platform this alert should go to (Sensu or Grafana)
    * @return
    * @example
    *   <pre> {@code .withHttp5xxThreshold(5) # alert when 5 requests return a 5xx status code in a 15-minute window .withHttp5xxThreshold(5,
    *   AlertSeverity.Warning) # raise a warning alert when 5 requests return a 5xx status code in a 15-minute window } </pre>
    */
  def withHttp5xxThreshold(http5xxThreshold: Int,
                           severity: AlertSeverity = AlertSeverity.Critical,
                           alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(http5xxThreshold = Http5xxThreshold(http5xxThreshold, severity, alertingPlatform))

  /** This alert will notify when the percentage of http responses returning a 5xx http status code exceeds a given threshold within a 15-minute
    * window.
    *
    * This alert is enabled by default at 100%, but can be disabled by setting it to >100.
    *
    * @param percentThreshold
    *   The %age of requests that must be 5xx to trigger the alarm
    * @param minimumHttp5xxCountThreshold
    *   The minimum count of 5xxs that must be present for the percentThreshold check to kick in. Optional. If you want to create a
    *   5xxPercentThreshold alert but only if you have a given count of 5xxs, this is the method to use. You want to use this parameter if, for
    *   example, you don't want to alert on just a one-off 5xx in the middle of the night Defaults to 0, which would mean the parameter has no effect.
    *   Only applies in Grafana-based alerting, NOT supported on Sensu.
    * @param severity
    *   How severe the alert is
    * @param alertingPlatform
    *   Which platform to direct the alert to
    * @return
    *   Configured threshold object
    * @example
    *   <pre> {@code .withHttp5xxPercentThreshold(25.0) # alert when 25% of all requests return a 5xx status code in a 15-minute window
    *   .withHttp5xxPercentThreshold(25.0, AlertSeverity.Warning) # raise a warning alert when 25% of all requests return a 5xx status code in a
    *   15-minute window .withHttp5xxPercentThreshold(25.0, 5, AlertSeverity.Warning) # raise a warning alert when 25% of all requests return a 5xx
    *   status code in a 15-minute window, where there are at least 5 total 5xx status codes observed in the time window } </pre>
    */
  def withHttp5xxPercentThreshold(percentThreshold: Double,
                                  minimumHttp5xxCountThreshold: Int = 0,
                                  severity: AlertSeverity = AlertSeverity.Critical,
                                  alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(http5xxPercentThreshold = Http5xxPercentThreshold(percentThreshold, minimumHttp5xxCountThreshold, severity, alertingPlatform))

  /** This alert will notify you when the 90th Percentile request time goes above the defined thresholds for the time period specified by the user.
    * @param threshold
    *   Object representing the response time in milliseconds above which alerts will be raised at given severities for a given time period
    * @return
    * @example
    *   <pre> {@code .withHttp90PercentileResponseTimeThreshold(Http90PercentileResponseTimeThreshold(warning = Some(1000), critical = Some(2000),
    *   timePeriod = 7)) } </pre>
    */
  def withHttp90PercentileResponseTimeThreshold(threshold: Http90PercentileResponseTimeThreshold) = {
    if (http90PercentileResponseTimeThresholds.nonEmpty) {
      throw new Exception("withHttp90PercentileResponseTimeThreshold has already been defined for this microservice")
    } else {
      this.copy(http90PercentileResponseTimeThresholds = Seq(threshold))
    }
  }

  /** @param threshold
    * @return
    * @example
    *   <pre> {@code .withHttpAbsolutePercentSplitThreshold(HttpAbsolutePercentSplitThreshold(100.0, Int.MaxValue, 40, 1.0, 2, "status:499")) } </pre>
    */
  def withHttpAbsolutePercentSplitThreshold(threshold: HttpAbsolutePercentSplitThreshold) =
    this.copy(httpAbsolutePercentSplitThresholds = httpAbsolutePercentSplitThresholds :+ threshold)

  /** @param threshold
    * @return
    * @example
    *   <pre> {@code .withHttpAbsolutePercentSplitDownstreamServiceThreshold(HttpAbsolutePercentSplitDownstreamServiceThreshold(10.0, 0, -1, 1.1, 2,
    *   "status:>498", "nps-hod-service",AlertSeverity.Critical)) } </pre>
    */
  def withHttpAbsolutePercentSplitDownstreamServiceThreshold(threshold: HttpAbsolutePercentSplitDownstreamServiceThreshold) =
    this.copy(httpAbsolutePercentSplitDownstreamServiceThresholds = httpAbsolutePercentSplitDownstreamServiceThresholds :+ threshold)

  /** @param threshold
    * @return
    * @example
    *   <pre> {@code .withHttpAbsolutePercentSplitDownstreamHodThreshold(HttpAbsolutePercentSplitDownstreamHodThreshold(10.0, 0, -1, 1.1, 2,
    *   "status:>498", "nps-hod-service",AlertSeverity.Critical)) } </pre>
    */
  def withHttpAbsolutePercentSplitDownstreamHodThreshold(threshold: HttpAbsolutePercentSplitDownstreamHodThreshold) =
    this.copy(httpAbsolutePercentSplitDownstreamHodThresholds = httpAbsolutePercentSplitDownstreamHodThresholds :+ threshold)

  /** This alert will notify when your microservice returns a specified number of requests with a given http status code (between 400 and 599) within
    * a 15-minute window.
    * @param threshold
    *   Object with fields httpStatus, count, severity and httpMethod
    * @return
    *
    * @example
    *   <pre> {@code .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_500, 5)) # alert when 5 occurences of status code 500 in a 15-minute
    *   window .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS(501), 5)) # alert when 5 occurences of status code 501 in a 15-minute window
    *   .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_502, 5, AlertSeverity.Warning)) # raise a warning alert when 5 occurences of status
    *   code 502 in a 15-minute window .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_503, 5, AlertSeverity.Warning, httpMethod.Post)) #
    *   raise a warning alert when 5 occurences of Post requests with response status code 503 in a 15-minute window } </pre>
    */
  def withHttpStatusThreshold(threshold: HttpStatusThreshold) =
    this.copy(httpStatusThresholds = httpStatusThresholds :+ threshold)

  /** This alert will notify you when the total number of requests received by the microservice is below a certain threshold.
    *
    * One or both of warning and critical must be given.
    *
    * @param threshold
    * @return
    * @example
    *   <pre> {@code .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_500, 5)) # alert when 5 occurences of status code 500 in a 15-minute
    *   window .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS(501), 5)) # alert when 5 occurences of status code 501 in a 15-minute window
    *   .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_502, 5, AlertSeverity.Warning)) # raise a warning alert when 5 occurences of status
    *   code 502 in a 15-minute window .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_503, 5, AlertSeverity.Warning, httpMethod.Post)) #
    *   raise a warning alert when 5 occurences of Post requests with response status code 503 in a 15-minute window }
    */
  def withHttpTrafficThreshold(threshold: HttpTrafficThreshold) = {
    if (httpTrafficThresholds.nonEmpty) {
      throw new Exception("withHttpTrafficThreshold has already been defined for this microservice")
    } else {
      this.copy(httpTrafficThresholds = Seq(threshold))
    }
  }

  /** This alert will notify when the percentage of http responses with a given http status code exceeds a given threshold within a 15-minute window.
    *
    * @param threshold
    * @return
    *   <pre> {@code .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS_404, 25.0)) # alert when 25% of all requests return status
    *   code 404 in a 15-minute window .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS_500, 25.0)) # alert when 25% of all
    *   requests return status code 500 in a 15-minute window .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS_504, 25.0,
    *   AlertSeverity.Warning, httpMethod.Post)) # raise a warning alert when 25% of all Post requests return status code 500 in a 15-minute window }
    *   </pre>
    */
  def withHttpStatusPercentThreshold(threshold: HttpStatusPercentThreshold) =
    this.copy(httpStatusPercentThresholds = httpStatusPercentThresholds :+ threshold)

  /** This alert will notify when a given metric query exceeds a given threshold within a 15-minute window.
    *
    * @param threshold
    * @return
    *   <pre> {@code .withMetricsThreshold(MetricsThreshold(name = "address_lookup_failed_audit", query =
    *   "sumSeries(play.address-lookup.ecs*.audit.failure.count)", warning = Some(4), critical = Some(6))) .withMetricsThreshold(MetricsThreshold(name
    *   \= "address_lookup_failed_reject_audit", query = "integral(sumSeries(play.address-lookup.ecs*.audit.{failure,reject}.count))", critical =
    *   Some(10))) } </pre>
    */
  def withMetricsThreshold(threshold: MetricsThreshold) =
    this.copy(metricsThresholds = metricsThresholds :+ threshold)

  /** All microservices are deployed to MDTP inside docker containers. If the docker container runs out of memory then the container will be killed
    * with an out-of-memory exception. This alert will notify when a specified number of containers are killed within a 15-minute window.
    *
    * @param containerCrashThreshold
    *   The number of container kills to alert on
    * @param alertingPlatform
    *   The that platform this alert should go to (Sensu or Grafana)
    * @return
    *   <pre> {@code .withContainerKillThreshold(5) # alert if 5 microservice instances are killed with an out-of-memory exception in a 15-minute
    *   window } </pre>
    */
  def withContainerKillThreshold(containerCrashThreshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(containerKillThreshold = ContainerKillThreshold(containerCrashThreshold, alertingPlatform))

  /** This alert will notify when your microservice receives a given number of requests within a 15-minute window.
    *
    * @param threshold
    *   The number of all http requests to alert on
    * @param alertingPlatform
    *   The that platform this alert should go to (Sensu or Grafana)
    * @return
    *   <pre> {@code .withTotalHttpRequestThreshold(1000) # alert when 1000 requests are received in a 15-minute window } </pre>
    */
  def withTotalHttpRequestThreshold(threshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(totalHttpRequestThreshold = TotalHttpRequestThreshold(threshold, alertingPlatform))

  /** This alert will notify when the count of a given log message is logged exceeds a given threshold within a 15-minute window.
    * @param message
    *   The substring to search for in the log message
    * @param threshold
    *   The threshold above which an alert will be raised
    * @param lessThanMode
    *   Flips the logic so that an alert is raised if less than the threshold amount is detected
    * @param severity
    *   The severity to set for this check in PagerDuty
    * @param alertingPlatform
    *   The that platform this alert should go to (Sensu or Grafana)
    * @return
    *   <pre> {@code .withLogMessageThreshold("Custom logs", 5) // occurrences of a specific log message in a 15-minute window
    *   .withLogMessageThreshold("heartbeat", 4, lessThanMode=true) // triggers if a specific log message appears less than 4 times in a 15-minute
    *   window .withLogMessageThreshold("MY_LOG_MESSAGE", 10, severity = AlertSeverity.Warning) // Raise warning alert in PagerDuty after 10 logs
    *   containing `MY_LOG_MESSAGE` are detected .withLogMessageThreshold("MY_LOG_MESSAGE", 2, alertingPlatform = AlertingPlatform.Grafan) // Raise an
    *   alert through Grafana when a service generates two or more logs containing the specified MY_LOG_MESSAGE within a 15-minute timeframe. }
    */
  def withLogMessageThreshold(message: String,
                              threshold: Int,
                              lessThanMode: Boolean = false,
                              severity: AlertSeverity = AlertSeverity.Critical,
                              alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(logMessageThresholds = logMessageThresholds :+ LogMessageThreshold(message, threshold, lessThanMode, severity, alertingPlatform))

  /** This alert will notify when the average CPU used by all instances of your microservice exceeds a given threshold within a 5-minute window.
    *
    * @param averageCPUThreshold
    *   The average percentage CPU used by all instances of your microservice
    * @param alertingPlatform
    *   The that platform this alert should go to (Sensu or Grafana)
    * @return
    * @example
    *   <pre> {@code .withAverageCPUThreshold(50) # alert if average CPU usage across all microservice instances is above 50% for the last 15 minutes
    *   } </pre>
    */
  def withAverageCPUThreshold(averageCPUThreshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(averageCPUThreshold = AverageCPUThreshold(averageCPUThreshold, alertingPlatform = alertingPlatform))

  /** In order to generate an alert for a service, alert-config expects an entry in app-config-\$ENV for that service. However, since platform
    * services don't have app-config entries by design, the creation of alerts for these services can be forced by adding .isPlatformService(true) to
    * your configBuilder.
    *
    * @deprecated
    *   Custom alerting coming soon!
    * @param platformService
    *   True if you are defining alerts for something that is NOT a standard microservice
    * @return
    */
  def isPlatformService(platformService: Boolean): AlertConfigBuilder =
    this.copy(platformService = platformService)

  def build: Option[String] = {
    import Http90PercentileResponseTimeThresholdProtocol._
    import LogMessageThresholdProtocol._
    import HttpTrafficThresholdProtocol._
    import HttpStatusThresholdProtocol._
    import HttpStatusPercentThresholdProtocol._
    import DefaultJsonProtocol._
    import AppConfigValidator._

    val currentEnvironment = Environment.get(System.getenv().getOrDefault("ENVIRONMENT", "production"))

    getAppConfigFileForService(serviceName, platformService).flatMap { file =>
      val serviceDomain = getZone(serviceName, file, platformService)

      def printSeq[A](a: Seq[A])(implicit writer: JsonFormat[A]): String =
        a.toJson.compactPrint

      ZoneToServiceDomainMapper.getServiceDomain(serviceDomain, platformService).map { serviceDomain =>
        val updated5xxPercentThreshold =
          if (isGrafanaEnabled(http5xxPercentThreshold.alertingPlatform, currentEnvironment, AlertType.Http5xxPercentThreshold)) {
            333.33
          } else {
            http5xxPercentThreshold.percentage
          }

        val updated5xxThreshold = if (isGrafanaEnabled(http5xxThreshold.alertingPlatform, currentEnvironment, AlertType.Http5xxThreshold)) {
          // if this alert is configured to use NOT Sensu, then set it to an unreasonably high threshold so
          // it will never be triggered
          Int.MaxValue
        } else {
          http5xxThreshold.count
        }

        val updatedAverageCPUThreshold =
          if (isGrafanaEnabled(averageCPUThreshold.alertingPlatform, currentEnvironment, AlertType.AverageCPUThreshold)) {
            Int.MaxValue
          } else {
            averageCPUThreshold.count
          }

        val updatedContainerKillThreshold =
          if (isGrafanaEnabled(containerKillThreshold.alertingPlatform, currentEnvironment, AlertType.ContainerKillThreshold)) {
            Int.MaxValue
          } else {
            containerKillThreshold.count
          }

        val updatedErrorsLoggedThreshold =
          if (isGrafanaEnabled(errorsLoggedThreshold.alertingPlatform, currentEnvironment, AlertType.ErrorsLoggedThreshold)) {
            Int.MaxValue
          } else {
            errorsLoggedThreshold.count
          }

        val updatedExceptionThreshold = if (isGrafanaEnabled(exceptionThreshold.alertingPlatform, currentEnvironment, AlertType.ExceptionThreshold)) {
          // if this alert is configured to use NOT Sensu, then set it to an unreasonably high threshold so
          // it will never be triggered
          Int.MaxValue
        } else {
          exceptionThreshold.count
        }

        val updatedTotalHttpRequestThreshold =
          if (isGrafanaEnabled(totalHttpRequestThreshold.alertingPlatform, currentEnvironment, AlertType.TotalHttpRequestThreshold)) {
            Int.MaxValue
          } else {
            totalHttpRequestThreshold.count
          }

        s"""
             |{
             |"app": "$serviceName.$serviceDomain",
             |"handlers": ${integrations.toJson.compactPrint},
             |"errors-logged-threshold":$updatedErrorsLoggedThreshold,
             |"exception-threshold":${exceptionThreshold.copy(count = updatedExceptionThreshold).toJson(ExceptionThresholdProtocol.thresholdFormat).compactPrint},
             |"5xx-threshold":${http5xxThreshold.copy(count = updated5xxThreshold).toJson(Http5xxThresholdProtocol.thresholdFormat).compactPrint},
             |"5xx-percent-threshold":${http5xxPercentThreshold.copy(percentage = updated5xxPercentThreshold).toJson(Http5xxPercentThresholdProtocol.thresholdFormat).compactPrint},
             |"containerKillThreshold" : $updatedContainerKillThreshold,
             |"http90PercentileResponseTimeThresholds" : ${http90PercentileResponseTimeThresholds.headOption.map(_.toJson.compactPrint).getOrElse(JsNull)},
             |"httpTrafficThresholds" : ${httpTrafficThresholds.filterNot(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.HttpTrafficThreshold)).toJson.compactPrint},
             |"httpStatusThresholds" : ${httpStatusThresholds.filterNot(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.HttpStatusThreshold)).toJson.compactPrint},
             |"httpStatusPercentThresholds" : ${httpStatusPercentThresholds.filterNot(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.HttpStatusPercentThreshold)).toJson.compactPrint},
             |"metricsThresholds" : ${printSeq(metricsThresholds.filterNot(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.MetricsThreshold)))(MetricsThresholdProtocol.thresholdFormat)},
             |"total-http-request-threshold": $updatedTotalHttpRequestThreshold,
             |"log-message-thresholds" : ${logMessageThresholds.filterNot(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.LogMessageThreshold)).toJson.compactPrint},
             |"average-cpu-threshold" : $updatedAverageCPUThreshold,
             |"absolute-percentage-split-threshold" : ${printSeq(httpAbsolutePercentSplitThresholds.filterNot(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.HttpAbsolutePercentSplitThreshold)))(HttpAbsolutePercentSplitThresholdProtocol.thresholdFormat)},
             |"absolute-percentage-split-downstream-service-threshold" : ${printSeq(httpAbsolutePercentSplitDownstreamServiceThresholds)(HttpAbsolutePercentSplitDownstreamServiceThresholdProtocol.thresholdFormat)},
             |"absolute-percentage-split-downstream-hod-threshold" : ${printSeq(httpAbsolutePercentSplitDownstreamHodThresholds.filterNot(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.HttpAbsolutePercentSplitDownstreamHodThreshold)))(HttpAbsolutePercentSplitDownstreamHodThresholdProtocol.thresholdFormat)}
             |}
              """.stripMargin
      }
    }
  }

}

case class TeamAlertConfigBuilder(
    services: Seq[String],
    integrations: Seq[String] = Seq("noop"),
    errorsLoggedThreshold: ErrorsLoggedThreshold = ErrorsLoggedThreshold(),
    exceptionThreshold: ExceptionThreshold = ExceptionThreshold(),
    http5xxThreshold: Http5xxThreshold = Http5xxThreshold(),
    http5xxPercentThreshold: Http5xxPercentThreshold = Http5xxPercentThreshold(100.0),
    http90PercentileResponseTimeThresholds: Seq[Http90PercentileResponseTimeThreshold] = Nil,
    httpAbsolutePercentSplitThresholds: Seq[HttpAbsolutePercentSplitThreshold] = Nil,
    httpAbsolutePercentSplitDownstreamServiceThresholds: Seq[HttpAbsolutePercentSplitDownstreamServiceThreshold] = Nil,
    httpAbsolutePercentSplitDownstreamHodThresholds: Seq[HttpAbsolutePercentSplitDownstreamHodThreshold] = Nil,
    containerKillThreshold: ContainerKillThreshold = ContainerKillThreshold(1),
    httpTrafficThresholds: Seq[HttpTrafficThreshold] = Nil,
    httpStatusThresholds: Seq[HttpStatusThreshold] = Nil,
    httpStatusPercentThresholds: Seq[HttpStatusPercentThreshold] = Nil,
    metricsThresholds: Seq[MetricsThreshold] = Nil,
    logMessageThresholds: Seq[LogMessageThreshold] = Nil,
    totalHttpRequestThreshold: TotalHttpRequestThreshold = TotalHttpRequestThreshold(),
    averageCPUThreshold: AverageCPUThreshold = AverageCPUThreshold(),
    platformService: Boolean = false
) extends Builder[Seq[AlertConfigBuilder]] {

  /** Sets integrations for the configuration, e.g. .withIntegrations("dass-cyclone")
    * @param string
    *   The name of your pagerduty integration. This integration must already exist in PagerDuty; alert-config will not create it for you.
    */
  def withIntegrations(integrations: String*) =
    this.copy(integrations = integrations)

  /** This alert will notify when your microservice logs a specified number of logs at ERROR log level within a 15-minute window.
    *
    * @param errorsLoggedThreshold
    *   The number of logs at ERROR level that this alert will trigger on
    * @param alertingPlatform
    *   The that platform this alert should go to (Sensu or Grafana)
    * @return
    * @example
    *   <pre> {@code .withErrorsLoggedThreshold(5) # alert when 5 logs at ERROR level are logged in a 15-minute window } </pre>
    */
  def withErrorsLoggedThreshold(errorsLoggedThreshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(errorsLoggedThreshold = ErrorsLoggedThreshold(errorsLoggedThreshold, alertingPlatform))

  /** This alert will notify when your microservice throws a specified number of exceptions, at log level ERROR, within a 15-minute window.
    * @param count
    *   The number of exceptions thrown that this alert will trigger on
    * @param alertingPlatform
    *   The that platform this alert should go to (Sensu or Grafana)
    * @return
    * @example
    *   <pre> {@code .withExceptionThreshold(count: Int, severity: AlertSeverity = AlertSeverity.Critical) } </pre>
    */
  def withExceptionThreshold(exceptionThreshold: Int,
                             severity: AlertSeverity = AlertSeverity.Critical,
                             alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(exceptionThreshold = ExceptionThreshold(exceptionThreshold, severity, alertingPlatform = alertingPlatform))

  /** This alert will notify when the number of http responses returning a 5xx http status code exceeds a given threshold within a 15-minute window.
    *
    * @param http5xxThreshold
    *   The number of all http responses with a 5xx status code to alert on
    * @param severity
    *   Whether to raise the alert as critical or warning
    * @param alertingPlatform
    *   The that platform this alert should go to (Sensu or Grafana)
    * @return
    * @example
    *   <pre> {@code .withHttp5xxThreshold(5) # alert when 5 requests return a 5xx status code in a 15-minute window .withHttp5xxThreshold(5,
    *   AlertSeverity.Warning) # raise a warning alert when 5 requests return a 5xx status code in a 15-minute window } </pre>
    */
  def withHttp5xxThreshold(http5xxThreshold: Int,
                           severity: AlertSeverity = AlertSeverity.Critical,
                           alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(http5xxThreshold = Http5xxThreshold(http5xxThreshold, severity, alertingPlatform))

  /** This alert will notify when the percentage of http responses returning a 5xx http status code exceeds a given threshold within a 15-minute
    * window.
    *
    * This alert is enabled by default at 100%, but can be disabled by setting it to >100.
    *
    * @param percentThreshold
    *   The %age of requests that must be 5xx to trigger the alarm
    * @param minimumHttp5xxCountThreshold
    *   The minimum count of 5xxs that must be present for the percentThreshold check to kick in. Optional. If you want to create a
    *   5xxPercentThreshold alert but only if you have a given count of 5xxs, this is the method to use. You want to use this parameter if, for
    *   example, you don't want to alert on just a one-off 5xx in the middle of the night Defaults to 0, which would mean the parameter has no effect.
    *   Only applies in Grafana-based alerting, NOT supported on Sensu.
    * @param severity
    *   How severe the alert is
    * @param alertingPlatform
    *   Which platform to direct the alert to
    * @return
    *   Configured threshold object
    * @example
    *   <pre> {@code .withHttp5xxPercentThreshold(25.0) # alert when 25% of all requests return a 5xx status code in a 15-minute window
    *   .withHttp5xxPercentThreshold(25.0, AlertSeverity.Warning) # raise a warning alert when 25% of all requests return a 5xx status code in a
    *   15-minute window .withHttp5xxPercentThreshold(25.0, 5, AlertSeverity.Warning) # raise a warning alert when 25% of all requests return a 5xx
    *   status code in a 15-minute window, where there are at least 5 total 5xx status codes observed in the time window } </pre>
    */
  def withHttp5xxPercentThreshold(percentThreshold: Double,
                                  minimumHttp5xxCountThreshold: Int = 0,
                                  severity: AlertSeverity = AlertSeverity.Critical,
                                  alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(http5xxPercentThreshold = Http5xxPercentThreshold(percentThreshold, minimumHttp5xxCountThreshold, severity, alertingPlatform))

  /** This alert will notify you when the 90th Percentile request time goes above the defined thresholds for the time period specified by the user.
    * @param threshold
    *   Object representing the response time in milliseconds above which alerts will be raised at given severities for a given time period
    * @return
    * @example
    *   <pre> {@code .withHttp90PercentileResponseTimeThreshold(Http90PercentileResponseTimeThreshold(warning = Some(1000), critical = Some(2000),
    *   timePeriod = 7)) } </pre>
    */
  def withHttp90PercentileResponseTimeThreshold(threshold: Http90PercentileResponseTimeThreshold) = {
    if (http90PercentileResponseTimeThresholds.nonEmpty) {
      throw new Exception("withHttp90PercentileResponseTimeThreshold has already been defined for this microservice")
    } else if (threshold.timePeriod <= 0 && threshold.timePeriod >= 15) {
      println(Console.CYAN + s" = ${threshold}" + Console.RESET)
      throw new Exception(
        s"withHttp90PercentileResponseTimeThreshold timePeriod '${threshold.timePeriod}' needs to be in the range 1-15 minutes (inclusive)")
    } else {
      this.copy(http90PercentileResponseTimeThresholds = Seq(threshold))
    }
  }

  /** @param threshold
    * @return
    * @example
    *   <pre> {@code .withHttpAbsolutePercentSplitThreshold(HttpAbsolutePercentSplitThreshold(100.0, Int.MaxValue, 40, 1.0, 2, "status:499")) } </pre>
    */
  def withHttpAbsolutePercentSplitThreshold(threshold: HttpAbsolutePercentSplitThreshold) =
    this.copy(httpAbsolutePercentSplitThresholds = httpAbsolutePercentSplitThresholds :+ threshold)

  def withHttpAbsolutePercentSplitDownstreamServiceThreshold(threshold: HttpAbsolutePercentSplitDownstreamServiceThreshold) =
    this.copy(httpAbsolutePercentSplitDownstreamServiceThresholds = httpAbsolutePercentSplitDownstreamServiceThresholds :+ threshold)

  def withHttpAbsolutePercentSplitDownstreamHodThreshold(threshold: HttpAbsolutePercentSplitDownstreamHodThreshold) =
    this.copy(httpAbsolutePercentSplitDownstreamHodThresholds = httpAbsolutePercentSplitDownstreamHodThresholds :+ threshold)

  /** All microservices are deployed to MDTP inside docker containers. If the docker container runs out of memory then the container will be killed
    * with an out-of-memory exception. This alert will notify when a specified number of containers are killed within a 15-minute window.
    *
    * @param containerCrashThreshold
    *   The number of container kills to alert on
    * @param alertingPlatform
    *   The that platform this alert should go to (Sensu or Grafana)
    * @return
    *   <pre> {@code .withContainerKillThreshold(5) # alert if 5 microservice instances are killed with an out-of-memory exception in a 15-minute
    *   window } </pre>
    */
  def withContainerKillThreshold(containerKillThreshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(containerKillThreshold = ContainerKillThreshold(containerKillThreshold, alertingPlatform))

  /** This alert will notify you when the total number of requests received by the microservice is below a certain threshold.
    *
    * One or both of warning and critical must be given.
    *
    * @param threshold
    * @return
    * @example
    *   <pre> {@code .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_500, 5)) # alert when 5 occurences of status code 500 in a 15-minute
    *   window .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS(501), 5)) # alert when 5 occurences of status code 501 in a 15-minute window
    *   .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_502, 5, AlertSeverity.Warning)) # raise a warning alert when 5 occurences of status
    *   code 502 in a 15-minute window .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_503, 5, AlertSeverity.Warning, httpMethod.Post)) #
    *   raise a warning alert when 5 occurences of Post requests with response status code 503 in a 15-minute window }
    */
  def withHttpTrafficThreshold(threshold: HttpTrafficThreshold) = {
    if (httpTrafficThresholds.nonEmpty) {
      throw new Exception("withHttpTrafficThreshold has already been defined for this microservice")
    } else {
      this.copy(httpTrafficThresholds = Seq(threshold))
    }
  }

  /** This alert will notify when your microservice returns a specified number of requests with a given http status code (between 400 and 599) within
    * a 15-minute window.
    * @param threshold
    *   Object with fields httpStatus, count, severity and httpMethod
    * @return
    *
    * @example
    *   <pre> {@code .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_500, 5)) # alert when 5 occurences of status code 500 in a 15-minute
    *   window .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS(501), 5)) # alert when 5 occurences of status code 501 in a 15-minute window
    *   .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_502, 5, AlertSeverity.Warning)) # raise a warning alert when 5 occurences of status
    *   code 502 in a 15-minute window .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_503, 5, AlertSeverity.Warning, httpMethod.Post)) #
    *   raise a warning alert when 5 occurences of Post requests with response status code 503 in a 15-minute window } </pre>
    */
  def withHttpStatusThreshold(threshold: HttpStatusThreshold) =
    this.copy(httpStatusThresholds = httpStatusThresholds :+ threshold)

  /** This alert will notify when the percentage of http responses with a given http status code exceeds a given threshold within a 15-minute window.
    *
    * @param threshold
    * @return
    *   <pre> {@code .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS_404, 25.0)) # alert when 25% of all requests return status
    *   code 404 in a 15-minute window .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS_500, 25.0)) # alert when 25% of all
    *   requests return status code 500 in a 15-minute window .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS_504, 25.0,
    *   AlertSeverity.Warning, httpMethod.Post)) # raise a warning alert when 25% of all Post requests return status code 500 in a 15-minute window }
    *   </pre>
    */
  def withHttpStatusPercentThreshold(threshold: HttpStatusPercentThreshold) =
    this.copy(httpStatusPercentThresholds = httpStatusPercentThresholds :+ threshold)

  /** This alert will notify when a given metric query exceeds a given threshold within a 15-minute window.
    *
    * @param threshold
    * @return
    *   <pre> {@code .withMetricsThreshold(MetricsThreshold(name = "address_lookup_failed_audit", query =
    *   "sumSeries(play.address-lookup.ecs*.audit.failure.count)", warning = Some(4), critical = Some(6))) .withMetricsThreshold(MetricsThreshold(name
    *   \= "address_lookup_failed_reject_audit", query = "integral(sumSeries(play.address-lookup.ecs*.audit.{failure,reject}.count))", critical =
    *   Some(10))) } </pre>
    */
  def withMetricsThreshold(threshold: MetricsThreshold) =
    this.copy(metricsThresholds = metricsThresholds :+ threshold)

  /** This alert will notify when your microservice receives a given number of requests within a 15-minute window.
    *
    * @param threshold
    *   The number of all http requests to alert on
    * @param alertingPlatform
    *   The that platform this alert should go to (Sensu or Grafana)
    * @return
    *   <pre> {@code .withTotalHttpRequestThreshold(1000) # alert when 1000 requests are received in a 15-minute window } </pre>
    */
  def withTotalHttpRequestThreshold(threshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(totalHttpRequestThreshold = TotalHttpRequestThreshold(threshold, alertingPlatform))

  /** This alert will notify when the count of a given log message is logged exceeds a given threshold within a 15-minute window.
    * @param message
    *   The substring to search for in the log message
    * @param threshold
    *   The threshold above which an alert will be raised
    * @param lessThanMode
    *   Flips the logic so that an alert is raised if less than the threshold amount is detected
    * @param severity
    *   The severity to set for this check in PagerDuty
    * @param alertingPlatform
    *   The that platform this alert should go to (Sensu or Grafana)
    * @return
    *   <pre> {@code .withLogMessageThreshold("Custom logs", 5) // occurrences of a specific log message in a 15-minute window
    *   .withLogMessageThreshold("heartbeat", 4, lessThanMode=true) // triggers if a specific log message appears less than 4 times in a 15-minute
    *   window .withLogMessageThreshold("MY_LOG_MESSAGE", 10, severity = AlertSeverity.Warning) // Raise warning alert in PagerDuty after 10 logs
    *   containing `MY_LOG_MESSAGE` are detected .withLogMessageThreshold("MY_LOG_MESSAGE", 2, alertingPlatform = AlertingPlatform.Grafan) // Raise an
    *   alert through Grafana when a service generates two or more logs containing the specified MY_LOG_MESSAGE within a 15-minute timeframe. }
    */
  def withLogMessageThreshold(message: String,
                              threshold: Int,
                              lessThanMode: Boolean = false,
                              severity: AlertSeverity = AlertSeverity.Critical,
                              alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(logMessageThresholds = logMessageThresholds :+ LogMessageThreshold(message, threshold, lessThanMode, severity, alertingPlatform))

  /** This alert will notify when the average CPU used by all instances of your microservice exceeds a given threshold within a 5-minute window.
    *
    * @param averageCPUThreshold
    *   The average percentage CPU used by all instances of your microservice
    * @param alertingPlatform
    *   The that platform this alert should go to (Sensu or Grafana)
    * @return
    * @example
    *   <pre> {@code .withAverageCPUThreshold(50) # alert if average CPU usage across all microservice instances is above 50% for the last 15 minutes
    *   } </pre>
    */
  def withAverageCPUThreshold(averageCPUThreshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(averageCPUThreshold = AverageCPUThreshold(averageCPUThreshold, alertingPlatform = alertingPlatform))

  /** In order to generate an alert for a service, alert-config expects an entry in app-config-\$ENV for that service. However, since platform
    * services don't have app-config entries by design, the creation of alerts for these services can be forced by adding .isPlatformService(true) to
    * your configBuilder.
    *
    * @deprecated
    *   Custom alerting coming soon!
    * @param platformService
    *   True if you are defining alerts for something that is NOT a standard microservice
    * @return
    */
  def isPlatformService(platformService: Boolean): TeamAlertConfigBuilder =
    this.copy(platformService = platformService)

  override def build: Seq[AlertConfigBuilder] =
    services.map(service =>
      AlertConfigBuilder(
        service,
        integrations,
        errorsLoggedThreshold,
        exceptionThreshold,
        http5xxThreshold,
        http5xxPercentThreshold,
        http90PercentileResponseTimeThresholds,
        httpAbsolutePercentSplitThresholds,
        httpAbsolutePercentSplitDownstreamServiceThresholds,
        httpAbsolutePercentSplitDownstreamHodThresholds,
        containerKillThreshold,
        httpTrafficThresholds,
        httpStatusThresholds,
        httpStatusPercentThresholds,
        metricsThresholds,
        logMessageThresholds,
        totalHttpRequestThreshold,
        averageCPUThreshold,
        platformService
      ))

}

object TeamAlertConfigBuilder {

  def teamAlerts(services: Seq[String]): TeamAlertConfigBuilder = {
    require(services.nonEmpty, "no alert service provided")
    TeamAlertConfigBuilder(services)
  }

}

object ZoneToServiceDomainMapper {
  val logger                       = new Logger()
  val zoneToServiceMappingFilePath = System.getProperty("zone-mapping-path", "zone-to-service-domain-mapping.yml")
  val zoneToServiceMappingFile     = new File(zoneToServiceMappingFilePath)

  if (!zoneToServiceMappingFile.exists())
    throw new FileNotFoundException(s"Could not find zone to service domain mapping file: ${zoneToServiceMappingFilePath}")

  val zoneToServiceDomainMappings: Map[String, String] =
    new Yaml()
      .load(new FileInputStream(zoneToServiceMappingFile))
      .asInstanceOf[java.util.Map[String, String]]
      .asScala
      .toMap

  def getServiceDomain(zone: Option[String], platformService: Boolean = false): Option[String] =
    if (platformService)
      Option("")
    else
      zone match {
        case Some(_: String) =>
          val mappedServiceDomain = zoneToServiceDomainMappings.get(zone.get)
          if (mappedServiceDomain.isEmpty) {
            logger.error(
              s"Zone to service domain mapping file '${zoneToServiceMappingFile.getAbsolutePath}' does not contain mapping for zone '${zone.get}'")
          }
          mappedServiceDomain
        case _ => zone
      }

}
