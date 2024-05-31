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

import uk.gov.hmrc.alertconfig.builder.Severity._
import uk.gov.hmrc.alertconfig.builder.yaml.YamlWriter.mapper
import uk.gov.hmrc.alertconfig.builder.{Environment, EnvironmentAlertBuilder, Logger}

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
        // This is a clumsy implementation of adding the INFO status.
        //
        // It must only be added for:
        // 1. the "team-telemetry-heartbeat-custom" integration
        // 2. in YAML (for Grafana)
        //
        // If we turn it on in Sensu, the entirety of Sensu crashes HARD! see TEL-4404
        if (builder.integrationName.equals("team-telemetry-heartbeat-custom")) {
          Integration(
            name = builder.integrationName,
            severitiesEnabled = Seq(Critical, Warning, Info).map(_.toString)
          )
        } else {
          Integration(
            name = builder.integrationName,
            severitiesEnabled = enabledSeverities.filter(Seq(Critical, Warning, Info).contains(_)).map(_.toString).toSeq
          )
        }
      }
    }.distinct

    mapper.writeValue(saveLocation, Integrations(enabledIntegrations))

    logger.debug(s"Done generating integrations YAML for $currentEnvironment")
  }

}
