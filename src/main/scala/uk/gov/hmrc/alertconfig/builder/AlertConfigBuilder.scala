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
    handlers: Seq[String] = Seq("noop"),
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

  def withHandlers(handlers: String*) =
    this.copy(handlers = handlers)

  def withErrorsLoggedThreshold(errorsLoggedThreshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(errorsLoggedThreshold = ErrorsLoggedThreshold(errorsLoggedThreshold, alertingPlatform))

  def withExceptionThreshold(exceptionThreshold: Int,
                             severity: AlertSeverity = AlertSeverity.Critical,
                             alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(exceptionThreshold = ExceptionThreshold(exceptionThreshold, severity, alertingPlatform = alertingPlatform))

  def withHttp5xxThreshold(http5xxThreshold: Int,
                           severity: AlertSeverity = AlertSeverity.Critical,
                           alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(http5xxThreshold = Http5xxThreshold(http5xxThreshold, severity, alertingPlatform))

  /**
   * @param percentThreshold The %age of requests that must be 5xx to trigger the alarm
   * @param minimumHttp5xxCountThreshold The minimum count of 5xxs that must be present for the percentThreshold check to kick in.
   * Optional.  If you want to create a 5xxPercentThreshold alert but only if you have a given count of 5xxs, this is the method
   * to use. You want to use this parameter if, for example, you don't want to alert on just a one-off 5xx in the middle of the night
   * @param severity How severe the alert is
   * @param alertingPlatform Which platform to direct the alert to
   * @return Configured threshold object
   */
  def withHttp5xxPercentThreshold(percentThreshold: Double,
                                  minimumHttp5xxCountThreshold: Int = -1,
                                  severity: AlertSeverity = AlertSeverity.Critical,
                                  alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(http5xxPercentThreshold = Http5xxPercentThreshold(percentThreshold, minimumHttp5xxCountThreshold, severity, alertingPlatform))

  def withHttp90PercentileResponseTimeThreshold(threshold: Http90PercentileResponseTimeThreshold) = {
    if (http90PercentileResponseTimeThresholds.nonEmpty) {
      throw new Exception("withHttp90PercentileResponseTimeThreshold has already been defined for this microservice")
    } else {
      this.copy(http90PercentileResponseTimeThresholds = Seq(threshold))
    }
  }

  def withHttpAbsolutePercentSplitThreshold(threshold: HttpAbsolutePercentSplitThreshold) =
    this.copy(httpAbsolutePercentSplitThresholds = httpAbsolutePercentSplitThresholds :+ threshold)

  def withHttpAbsolutePercentSplitDownstreamServiceThreshold(threshold: HttpAbsolutePercentSplitDownstreamServiceThreshold) =
    this.copy(httpAbsolutePercentSplitDownstreamServiceThresholds = httpAbsolutePercentSplitDownstreamServiceThresholds :+ threshold)

  def withHttpAbsolutePercentSplitDownstreamHodThreshold(threshold: HttpAbsolutePercentSplitDownstreamHodThreshold) =
    this.copy(httpAbsolutePercentSplitDownstreamHodThresholds = httpAbsolutePercentSplitDownstreamHodThresholds :+ threshold)

  def withHttpStatusThreshold(threshold: HttpStatusThreshold) =
    this.copy(httpStatusThresholds = httpStatusThresholds :+ threshold)

  def withHttpTrafficThreshold(threshold: HttpTrafficThreshold) = {
    if (httpTrafficThresholds.nonEmpty) {
      throw new Exception("withHttpTrafficThreshold has already been defined for this microservice")
    } else {
      this.copy(httpTrafficThresholds = Seq(threshold))
    }
  }

  def withHttpStatusPercentThreshold(threshold: HttpStatusPercentThreshold) =
    this.copy(httpStatusPercentThresholds = httpStatusPercentThresholds :+ threshold)

  def withMetricsThreshold(threshold: MetricsThreshold) =
    this.copy(metricsThresholds = metricsThresholds :+ threshold)

  def withContainerKillThreshold(containerCrashThreshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(containerKillThreshold = ContainerKillThreshold(containerCrashThreshold, alertingPlatform))

  def withTotalHttpRequestsCountThreshold(threshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(totalHttpRequestThreshold = TotalHttpRequestThreshold(threshold, alertingPlatform))

  def withLogMessageThreshold(message: String,
                              threshold: Int,
                              lessThanMode: Boolean = false,
                              severity: AlertSeverity = AlertSeverity.Critical,
                              alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(logMessageThresholds = logMessageThresholds :+ LogMessageThreshold(message, threshold, lessThanMode, severity, alertingPlatform))

  def withAverageCPUThreshold(averageCPUThreshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(averageCPUThreshold = AverageCPUThreshold(averageCPUThreshold, alertingPlatform = alertingPlatform))

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
          val updated5xxPercentThreshold = if (isGrafanaEnabled(http5xxPercentThreshold.alertingPlatform, currentEnvironment, AlertType.Http5xxPercentThreshold)) {
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

          val updatedAverageCPUThreshold = if (isGrafanaEnabled(averageCPUThreshold.alertingPlatform, currentEnvironment, AlertType.AverageCPUThreshold)) {
            Int.MaxValue
          } else {
            averageCPUThreshold.count
          }

          val updatedContainerKillThreshold = if (isGrafanaEnabled(containerKillThreshold.alertingPlatform, currentEnvironment, AlertType.ContainerKillThreshold)) {
            Int.MaxValue
          } else {
            containerKillThreshold.count
          }

          val updatedErrorsLoggedThreshold = if (isGrafanaEnabled(errorsLoggedThreshold.alertingPlatform, currentEnvironment, AlertType.ErrorsLoggedThreshold)) {
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

          val updatedTotalHttpRequestThreshold = if (isGrafanaEnabled(totalHttpRequestThreshold.alertingPlatform, currentEnvironment, AlertType.TotalHttpRequestThreshold)) {
            Int.MaxValue
          } else {
            totalHttpRequestThreshold.count
          }

          s"""
             |{
             |"app": "$serviceName.$serviceDomain",
             |"handlers": ${handlers.toJson.compactPrint},
             |"errors-logged-threshold":$updatedErrorsLoggedThreshold,
             |"exception-threshold":${exceptionThreshold
              .copy(count = updatedExceptionThreshold)
              .toJson(ExceptionThresholdProtocol.thresholdFormat)
              .compactPrint},
             |"5xx-threshold":${http5xxThreshold
              .copy(count = updated5xxThreshold)
              .toJson(Http5xxThresholdProtocol.thresholdFormat)
              .compactPrint},
             |"5xx-percent-threshold":${http5xxPercentThreshold
              .copy(percentage = updated5xxPercentThreshold)
              .toJson(Http5xxPercentThresholdProtocol.thresholdFormat)
              .compactPrint},
             |"containerKillThreshold" : $updatedContainerKillThreshold,
             |"http90PercentileResponseTimeThresholds" : ${http90PercentileResponseTimeThresholds.headOption
              .map(_.toJson.compactPrint)
              .getOrElse(JsNull)},
             |"httpTrafficThresholds" : ${httpTrafficThresholds.filterNot(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.HttpTrafficThreshold)).toJson.compactPrint},
             |"httpStatusThresholds" : ${httpStatusThresholds.filterNot(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.HttpStatusThreshold)).toJson.compactPrint},
             |"httpStatusPercentThresholds" : ${httpStatusPercentThresholds.filterNot(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.HttpStatusPercentThreshold)).toJson.compactPrint},
             |"metricsThresholds" : ${printSeq(metricsThresholds.filterNot(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.MetricsThreshold)))(
              MetricsThresholdProtocol.thresholdFormat)},
             |"total-http-request-threshold": $updatedTotalHttpRequestThreshold,
             |"log-message-thresholds" : ${logMessageThresholds.filterNot(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.LogMessageThreshold)).toJson.compactPrint},
             |"average-cpu-threshold" : $updatedAverageCPUThreshold,
             |"absolute-percentage-split-threshold" : ${printSeq(httpAbsolutePercentSplitThresholds.filterNot(a => isGrafanaEnabled(a.alertingPlatform, currentEnvironment, AlertType.HttpAbsolutePercentSplitThreshold)))(HttpAbsolutePercentSplitThresholdProtocol.thresholdFormat)},
             |"absolute-percentage-split-downstream-service-threshold" : ${printSeq(httpAbsolutePercentSplitDownstreamServiceThresholds)(
              HttpAbsolutePercentSplitDownstreamServiceThresholdProtocol.thresholdFormat)},
             |"absolute-percentage-split-downstream-hod-threshold" : ${printSeq(httpAbsolutePercentSplitDownstreamHodThresholds)(
              HttpAbsolutePercentSplitDownstreamHodThresholdProtocol.thresholdFormat)}
             |}
              """.stripMargin
        }
    }
  }

}

case class TeamAlertConfigBuilder(
    services: Seq[String],
    handlers: Seq[String] = Seq("noop"),
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

  def withHandlers(handlers: String*) =
    this.copy(handlers = handlers)

  def withErrorsLoggedThreshold(errorsLoggedThreshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(errorsLoggedThreshold = ErrorsLoggedThreshold(errorsLoggedThreshold, alertingPlatform))

  def withExceptionThreshold(exceptionThreshold: Int,
                             severity: AlertSeverity = AlertSeverity.Critical,
                             alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(exceptionThreshold = ExceptionThreshold(exceptionThreshold, severity, alertingPlatform = alertingPlatform))

  def withHttp5xxThreshold(http5xxThreshold: Int,
                           severity: AlertSeverity = AlertSeverity.Critical,
                           alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(http5xxThreshold = Http5xxThreshold(http5xxThreshold, severity, alertingPlatform))

  /**
   * @param percentThreshold The %age of requests that must be 5xx to trigger the alarm
   * @param minimumHttp5xxCountThreshold The minimum count of 5xxs that must be present for the percentThreshold check to kick in.
   * Optional.  If you want to create a 5xxPercentThreshold alert but only if you have a given count of 5xxs, this is the method
   * to use. You want to use this parameter if, for example, you don't want to alert on just a one-off 5xx in the middle of the night
   * @param severity How severe the alert is
   * @param alertingPlatform Which platform to direct the alert to
   * @return Configured threshold object
   */
  def withHttp5xxPercentThreshold(percentThreshold: Double,
                                  minimumHttp5xxCountThreshold: Int = -1,
                                  severity: AlertSeverity = AlertSeverity.Critical,
                                  alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(http5xxPercentThreshold = Http5xxPercentThreshold(percentThreshold, minimumHttp5xxCountThreshold, severity, alertingPlatform))

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

  def withHttpAbsolutePercentSplitThreshold(threshold: HttpAbsolutePercentSplitThreshold) =
    this.copy(httpAbsolutePercentSplitThresholds = httpAbsolutePercentSplitThresholds :+ threshold)

  def withHttpAbsolutePercentSplitDownstreamServiceThreshold(threshold: HttpAbsolutePercentSplitDownstreamServiceThreshold) =
    this.copy(httpAbsolutePercentSplitDownstreamServiceThresholds = httpAbsolutePercentSplitDownstreamServiceThresholds :+ threshold)

  def withHttpAbsolutePercentSplitDownstreamHodThreshold(threshold: HttpAbsolutePercentSplitDownstreamHodThreshold) =
    this.copy(httpAbsolutePercentSplitDownstreamHodThresholds = httpAbsolutePercentSplitDownstreamHodThresholds :+ threshold)

  def withContainerKillThreshold(containerKillThreshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(containerKillThreshold = ContainerKillThreshold(containerKillThreshold, alertingPlatform))

  def withHttpTrafficThreshold(threshold: HttpTrafficThreshold) = {
    if (httpTrafficThresholds.nonEmpty) {
      throw new Exception("withHttpTrafficThreshold has already been defined for this microservice")
    } else {
      this.copy(httpTrafficThresholds = Seq(threshold))
    }
  }

  def withHttpStatusThreshold(threshold: HttpStatusThreshold) =
    this.copy(httpStatusThresholds = httpStatusThresholds :+ threshold)

  def withHttpStatusPercentThreshold(threshold: HttpStatusPercentThreshold) =
    this.copy(httpStatusPercentThresholds = httpStatusPercentThresholds :+ threshold)

  def withMetricsThreshold(threshold: MetricsThreshold) =
    this.copy(metricsThresholds = metricsThresholds :+ threshold)

  def withTotalHttpRequestsCountThreshold(threshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(totalHttpRequestThreshold = TotalHttpRequestThreshold(threshold, alertingPlatform))

  def withLogMessageThreshold(message: String,
                              threshold: Int,
                              lessThanMode: Boolean = false,
                              severity: AlertSeverity = AlertSeverity.Critical,
                              alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(logMessageThresholds = logMessageThresholds :+ LogMessageThreshold(message, threshold, lessThanMode, severity, alertingPlatform))

  def withAverageCPUThreshold(averageCPUThreshold: Int, alertingPlatform: AlertingPlatform = AlertingPlatform.Default) =
    this.copy(averageCPUThreshold = AverageCPUThreshold(averageCPUThreshold, alertingPlatform = alertingPlatform))

  def isPlatformService(platformService: Boolean): TeamAlertConfigBuilder =
    this.copy(platformService = platformService)

  override def build: Seq[AlertConfigBuilder] =
    services.map(service =>
      AlertConfigBuilder(
        service,
        handlers,
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
