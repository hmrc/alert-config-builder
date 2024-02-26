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

import uk.gov.hmrc.alertconfig.builder.yaml.YamlWriter.mapper
import uk.gov.hmrc.alertconfig.builder.{Environment, EnvironmentAlertBuilder, Logger, Severity}

import java.io.File
import scala.collection.immutable.Seq

object IntegrationsYamlBuilder {

  val logger = new Logger()

  def generate(environmentAlertBuilders: Seq[EnvironmentAlertBuilder], currentEnvironment: Environment, saveLocation: File): Unit = {
    logger.debug(s"Generating integrations YAML for $currentEnvironment")
    val enabledIntegrations = environmentAlertBuilders.flatMap { builder =>
      val enabledEnvironments = builder.enabledEnvironments
      Option.when(enabledEnvironments.contains(currentEnvironment)) {
        val enabledSeverities = enabledEnvironments(currentEnvironment)
        Integration(
          name = builder.handlerName,
          SeveritiesEnabled(
            warning = enabledSeverities.contains(Severity.Warning),
            critical = enabledSeverities.contains(Severity.Critical)
          )
        )
      }
    }.distinct

    mapper.writeValue(saveLocation, Integrations(enabledIntegrations))

    logger.debug(s"Done generating integrations YAML for $currentEnvironment")
  }

}
