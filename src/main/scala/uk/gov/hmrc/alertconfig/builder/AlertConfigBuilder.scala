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

import java.io.{File, FileInputStream, FileNotFoundException}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

trait Builder[T] {
  def build: T
}

case class AlertConfigBuilder(
  serviceName                                        : String,
  handlers                                           : Seq[String]                                             = Seq("noop"),
  errorsLoggedThreshold                              : Int                                                     = Int.MaxValue,
  exceptionThreshold                                 : ExceptionThreshold                                      = ExceptionThreshold(),
  http5xxThreshold                                   : Http5xxThreshold                                        = Http5xxThreshold(),
  http5xxPercentThreshold                            : Http5xxPercentThreshold                                 = Http5xxPercentThreshold(100.0),
  httpAbsolutePercentSplitThresholds                 : Seq[HttpAbsolutePercentSplitThreshold]                  = Nil,
  httpAbsolutePercentSplitDownstreamServiceThresholds: Seq[HttpAbsolutePercentSplitDownstreamServiceThreshold] = Nil,
  httpAbsolutePercentSplitDownstreamHodThresholds    : Seq[HttpAbsolutePercentSplitDownstreamHodThreshold]     = Nil,
  containerKillThreshold                             : Int                                                     = 1,
  httpStatusThresholds                               : Seq[HttpStatusThreshold]                                = Nil,
  httpStatusPercentThresholds                        : Seq[HttpStatusPercentThreshold]                         = Nil,
  metricsThresholds                                  : Seq[MetricsThreshold]                                   = Nil,
  logMessageThresholds                               : Seq[LogMessageThreshold]                                = Nil,
  totalHttpRequestThreshold                          : Int                                                     = Int.MaxValue,
  averageCPUThreshold                                : Int                                                     = Int.MaxValue,
  platformService                                    : Boolean                                                 = false
) extends Builder[Option[String]] {

  import spray.json._

  val logger = new Logger()

  def withHandlers(handlers: String*) =
    this.copy(handlers = handlers)

  def withErrorsLoggedThreshold(errorsLoggedThreshold: Int) =
    this.copy(errorsLoggedThreshold = errorsLoggedThreshold)

  def withExceptionThreshold(exceptionThreshold: Int, severity: AlertSeverity = AlertSeverity.Critical) =
    this.copy(exceptionThreshold = ExceptionThreshold(exceptionThreshold, severity))

  def withHttp5xxThreshold(http5xxThreshold: Int, severity: AlertSeverity = AlertSeverity.Critical) =
    this.copy(http5xxThreshold = Http5xxThreshold(http5xxThreshold, severity))

  def withHttp5xxPercentThreshold(percentThreshold: Double, severity: AlertSeverity = AlertSeverity.Critical) =
    this.copy(http5xxPercentThreshold = Http5xxPercentThreshold(percentThreshold, severity))

  def withHttpAbsolutePercentSplitThreshold(threshold: HttpAbsolutePercentSplitThreshold) =
    this.copy(httpAbsolutePercentSplitThresholds = httpAbsolutePercentSplitThresholds :+ threshold)

  def withHttpAbsolutePercentSplitDownstreamServiceThreshold(threshold: HttpAbsolutePercentSplitDownstreamServiceThreshold) =
    this.copy(httpAbsolutePercentSplitDownstreamServiceThresholds = httpAbsolutePercentSplitDownstreamServiceThresholds :+ threshold)

  def withHttpAbsolutePercentSplitDownstreamHodThreshold(threshold: HttpAbsolutePercentSplitDownstreamHodThreshold) =
    this.copy(httpAbsolutePercentSplitDownstreamHodThresholds = httpAbsolutePercentSplitDownstreamHodThresholds :+ threshold)

  def withHttpStatusThreshold(threshold: HttpStatusThreshold) =
    this.copy(httpStatusThresholds = httpStatusThresholds :+ threshold)

  def withHttpStatusPercentThreshold(threshold: HttpStatusPercentThreshold) =
    this.copy(httpStatusPercentThresholds = httpStatusPercentThresholds :+ threshold)

  def withMetricsThreshold(threshold: MetricsThreshold) =
    this.copy(metricsThresholds = metricsThresholds :+ threshold)

  def withContainerKillThreshold(containerCrashThreshold: Int) =
    this.copy(containerKillThreshold = containerCrashThreshold)

  def withLogMessageThreshold(message: String, threshold: Int, lessThanMode: Boolean = false) =
    this.copy(logMessageThresholds = logMessageThresholds :+ LogMessageThreshold(message, threshold, lessThanMode))

  def withAverageCPUThreshold(averageCPUThreshold: Int) =
    this.copy(averageCPUThreshold = averageCPUThreshold)

  def isPlatformService(platformService: Boolean): AlertConfigBuilder =
    this.copy(platformService = platformService)

  def build: Option[String] = {
    import HttpStatusThresholdProtocol._
    import HttpStatusPercentThresholdProtocol._
    import DefaultJsonProtocol._


    val appConfigPath = System.getProperty("app-config-path", "../app-config")
    val appConfigDirectory = new File(appConfigPath)
    val appConfigFile = new File(appConfigDirectory, s"${serviceName}.yaml")

    if (!appConfigDirectory.exists)
      throw new FileNotFoundException(s"Could not find app-config repository: $appConfigPath")


    appConfigFile match {
      case file if !platformService && !file.exists =>
        logger.info(s"No app-config file found for service: '${serviceName}'. File was expected at: '${file.getAbsolutePath}'")
        None
      case file if !platformService && getZone(file).isEmpty =>
        logger.warn(s"app-config file for service: '${serviceName}' does not contain 'zone' key.")
        None
      case file =>
        val serviceDomain = getZone(file, platformService)

        def printSeq[A](a: Seq[A])(implicit writer: JsonFormat[A]): String =
          a.toJson.compactPrint

        ZoneToServiceDomainMapper.getServiceDomain(serviceDomain, platformService).map { serviceDomain =>
          s"""
             |{
             |"app": "$serviceName.$serviceDomain",
             |"handlers": ${handlers.toJson.compactPrint},
             |"errors-logged-threshold":$errorsLoggedThreshold,
             |"exception-threshold":${exceptionThreshold.toJson(ExceptionThresholdProtocol.thresholdFormat).compactPrint},
             |"5xx-threshold":${http5xxThreshold.toJson(Http5xxThresholdProtocol.thresholdFormat).compactPrint},
             |"5xx-percent-threshold":${http5xxPercentThreshold.toJson(Http5xxPercentThresholdProtocol.thresholdFormat).compactPrint},
             |"containerKillThreshold" : $containerKillThreshold,
             |"httpStatusThresholds" : ${httpStatusThresholds.toJson.compactPrint},
             |"httpStatusPercentThresholds" : ${httpStatusPercentThresholds.toJson.compactPrint},
             |"metricsThresholds" : ${printSeq(metricsThresholds)(MetricsThresholdProtocol.thresholdFormat)},
             |"total-http-request-threshold": $totalHttpRequestThreshold,
             |"log-message-thresholds" : $buildLogMessageThresholdsJson,
             |"average-cpu-threshold" : $averageCPUThreshold,
             |"absolute-percentage-split-threshold" : ${printSeq(httpAbsolutePercentSplitThresholds)(HttpAbsolutePercentSplitThresholdProtocol.thresholdFormat)},
             |"absolute-percentage-split-downstream-service-threshold" : ${printSeq(httpAbsolutePercentSplitDownstreamServiceThresholds)(HttpAbsolutePercentSplitDownstreamServiceThresholdProtocol.thresholdFormat)},
             |"absolute-percentage-split-downstream-hod-threshold" : ${printSeq(httpAbsolutePercentSplitDownstreamHodThresholds)(HttpAbsolutePercentSplitDownstreamHodThresholdProtocol.thresholdFormat)}
             |}
              """.stripMargin
        }
    }
  }

  def buildLogMessageThresholdsJson = {
    import LogMessageThresholdProtocol._
    logMessageThresholds.toJson.compactPrint
  }

  def getZone(appConfigFile: File, platformService: Boolean = false): Option[String] =
    if (platformService)
      None
    else {
      def parseAppConfigFile: Try[java.util.Map[String, java.util.Map[String, String]]] =
        Try(new Yaml().load(new FileInputStream(appConfigFile)).asInstanceOf[java.util.Map[String, java.util.Map[String, String]]])

      parseAppConfigFile match {
        case Failure(exception) =>
          logger.warn(s"app-config file ${appConfigFile} for service: '${serviceName}' is not valid YAML and could not be parsed. Parsing Exception: ${exception.getMessage}")
          None
        case Success(appConfig) =>
          val versionObject = appConfig.asScala.toMap.view.mapValues(_.asScala.toMap)("0.0.0")
          versionObject.get("zone")
      }
    }
}

case class TeamAlertConfigBuilder(
  services                                           : Seq[String],
  handlers                                           : Seq[String]                                             = Seq("noop"),
  errorsLoggedThreshold                              : Int                                                     = Int.MaxValue,
  exceptionThreshold                                 : ExceptionThreshold                                      = ExceptionThreshold(),
  http5xxThreshold                                   : Http5xxThreshold                                        = Http5xxThreshold(),
  http5xxPercentThreshold                            : Http5xxPercentThreshold                                 = Http5xxPercentThreshold(100.0),
  httpAbsolutePercentSplitThresholds                 : Seq[HttpAbsolutePercentSplitThreshold]                  = Nil,
  httpAbsolutePercentSplitDownstreamServiceThresholds: Seq[HttpAbsolutePercentSplitDownstreamServiceThreshold] = Nil,
  httpAbsolutePercentSplitDownstreamHodThresholds    : Seq[HttpAbsolutePercentSplitDownstreamHodThreshold]     = Nil,
  containerKillThreshold                             : Int                                                     = 1,
  httpStatusThresholds                               : Seq[HttpStatusThreshold]                                = Nil,
  httpStatusPercentThresholds                        : Seq[HttpStatusPercentThreshold]                         = Nil,
  metricsThresholds                                  : Seq[MetricsThreshold]                                   = Nil,
  logMessageThresholds                               : Seq[LogMessageThreshold]                                = Nil,
  totalHttpRequestThreshold                          : Int                                                     = Int.MaxValue,
  averageCPUThreshold                                : Int                                                     = Int.MaxValue,
  platformService                                    : Boolean                                                 = false
) extends Builder[Seq[AlertConfigBuilder]] {

  def withHandlers(handlers: String*) =
    this.copy(handlers = handlers)

  def withErrorsLoggedThreshold(errorsLoggedThreshold: Int) =
    this.copy(errorsLoggedThreshold = errorsLoggedThreshold)

  def withExceptionThreshold(exceptionThreshold: Int, severity: AlertSeverity = AlertSeverity.Critical) =
    this.copy(exceptionThreshold = ExceptionThreshold(exceptionThreshold, severity))

  def withHttp5xxThreshold(http5xxThreshold: Int, severity: AlertSeverity = AlertSeverity.Critical) =
    this.copy(http5xxThreshold = Http5xxThreshold(http5xxThreshold, severity))

  def withHttp5xxPercentThreshold(percentThreshold: Double, severity: AlertSeverity = AlertSeverity.Critical) =
    this.copy(http5xxPercentThreshold = Http5xxPercentThreshold(percentThreshold, severity))

  def withHttpAbsolutePercentSplitThreshold(threshold: HttpAbsolutePercentSplitThreshold) =
    this.copy(httpAbsolutePercentSplitThresholds = httpAbsolutePercentSplitThresholds :+ threshold)

  def withHttpAbsolutePercentSplitDownstreamServiceThreshold(threshold: HttpAbsolutePercentSplitDownstreamServiceThreshold) =
    this.copy(httpAbsolutePercentSplitDownstreamServiceThresholds = httpAbsolutePercentSplitDownstreamServiceThresholds :+ threshold)

  def withHttpAbsolutePercentSplitDownstreamHodThreshold(threshold: HttpAbsolutePercentSplitDownstreamHodThreshold) =
    this.copy(httpAbsolutePercentSplitDownstreamHodThresholds = httpAbsolutePercentSplitDownstreamHodThresholds :+ threshold)

  def withContainerKillThreshold(containerKillThreshold: Int) =
    this.copy(containerKillThreshold = containerKillThreshold)

  def withHttpStatusThreshold(threshold: HttpStatusThreshold) =
    this.copy(httpStatusThresholds = httpStatusThresholds :+ threshold)

  def withHttpStatusPercentThreshold(threshold: HttpStatusPercentThreshold) =
    this.copy(httpStatusPercentThresholds = httpStatusPercentThresholds :+ threshold)

  def withMetricsThreshold(threshold: MetricsThreshold) =
    this.copy(metricsThresholds = metricsThresholds :+ threshold)

  def withTotalHttpRequestsCountThreshold(threshold: Int) =
    this.copy(totalHttpRequestThreshold = threshold)

  def withLogMessageThreshold(message: String, threshold: Int, lessThanMode: Boolean = false) =
    this.copy(logMessageThresholds = logMessageThresholds :+ LogMessageThreshold(message, threshold, lessThanMode))

  def withAverageCPUThreshold(averageCPUThreshold: Int) =
    this.copy(averageCPUThreshold = averageCPUThreshold)

  def isPlatformService(platformService: Boolean): TeamAlertConfigBuilder =
    this.copy(platformService = platformService)

  override def build: Seq[AlertConfigBuilder] =
    services.map(service =>
      AlertConfigBuilder(service,
        handlers,
        errorsLoggedThreshold,
        exceptionThreshold,
        http5xxThreshold,
        http5xxPercentThreshold,
        httpAbsolutePercentSplitThresholds,
        httpAbsolutePercentSplitDownstreamServiceThresholds,
        httpAbsolutePercentSplitDownstreamHodThresholds,
        containerKillThreshold,
        httpStatusThresholds,
        httpStatusPercentThresholds,
        metricsThresholds,
        logMessageThresholds,
        totalHttpRequestThreshold,
        averageCPUThreshold,
        platformService)
    )
}

object TeamAlertConfigBuilder {
  def teamAlerts(services: Seq[String]): TeamAlertConfigBuilder = {
    require(services.nonEmpty, "no alert service provided")
    TeamAlertConfigBuilder(services)
  }
}

object ZoneToServiceDomainMapper {
  val logger = new Logger()
  val zoneToServiceMappingFilePath = System.getProperty("zone-mapping-path", "zone-to-service-domain-mapping.yml")
  val zoneToServiceMappingFile = new File(zoneToServiceMappingFilePath)
  if (!zoneToServiceMappingFile.exists())
    throw new FileNotFoundException(s"Could not find zone to service domain mapping file: ${zoneToServiceMappingFilePath}")

  val zoneToServiceDomainMappings: Map[String, String] =
    new Yaml()
      .load(new FileInputStream(zoneToServiceMappingFile)).asInstanceOf[java.util.Map[String, String]]
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
            logger.error(s"Zone to service domain mapping file '${zoneToServiceMappingFile.getAbsolutePath}' does not contain mapping for zone '${zone.get}'")
          }
          mappedServiceDomain
        case _ => zone
      }
}