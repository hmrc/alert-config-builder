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

package uk.gov.hmrc.alertconfig.builder

import org.yaml.snakeyaml.Yaml

import java.io.{File, FileInputStream, FileNotFoundException}
import scala.util.{Failure, Success, Try}
import scala.jdk.CollectionConverters._

object AppConfigValidator {
  val logger = new Logger()

  def getAppConfigFileForService(serviceName: String, platformService: Boolean): Option[File] = {
    val appConfigPath      = System.getProperty("app-config-path", "../app-config")
    val appConfigDirectory = new File(appConfigPath)
    val appConfigFile      = new File(appConfigDirectory, s"${serviceName}.yaml")

    if (!appConfigDirectory.exists)
      throw new FileNotFoundException(s"Could not find app-config repository: $appConfigPath")

    appConfigFile match {
      case file if !platformService && !file.exists =>
        logger.info(s"No app-config file found for service: '${serviceName}'. File was expected at: '${file.getAbsolutePath}'")
        None
      case file if !platformService && getZone(serviceName, file).isEmpty =>
        logger.warn(s"app-config file for service: '${serviceName}' does not contain 'zone' key.")
        None
      case file => Some(file)
    }
  }

  def serviceDeployedInEnv(serviceName: String, platformService: Boolean): Boolean =
    getAppConfigFileForService(serviceName, platformService) match {
      case Some(file) => true
      case None       => false
    }

  def getZone(serviceName: String, appConfigFile: File, platformService: Boolean = false): Option[String] =
    if (platformService)
      None
    else {
      def parseAppConfigFile: Try[java.util.Map[String, java.util.Map[String, String]]] =
        Try(new Yaml().load(new FileInputStream(appConfigFile)).asInstanceOf[java.util.Map[String, java.util.Map[String, String]]])

      parseAppConfigFile match {
        case Failure(exception) =>
          logger.warn(
            s"app-config file ${appConfigFile} for service: '${serviceName}' is not valid YAML and could not be parsed. Parsing Exception: ${exception.getMessage}")
          None
        case Success(appConfig) =>
          val versionObject = appConfig.asScala.toMap.view.mapValues(_.asScala.toMap)("0.0.0")
          versionObject.get("zone")
      }
    }

}
