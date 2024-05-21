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

package uk.gov.hmrc.alertconfig.builder.custom

import uk.gov.hmrc.alertconfig.builder.Environment
import uk.gov.hmrc.alertconfig.builder.yaml.YamlWriter.mapper

import java.io.File

case class CustomAlertsTopLevel(alerts: CustomAlerts)

case class CustomAlerts(
    customLogAlerts: Seq[CustomLogAlert],
    customGraphiteMetricAlert: Seq[CustomGraphiteMetricAlert],
    customCloudWatchMetricAlerts: Seq[CustomCloudWatchMetricAlert]
)

object CustomAlertConfigYamlBuilder {

  def run(customAlertConfigs: Seq[CustomAlertConfig], environment: String, saveLocation: File): Unit = {

    val customAlerts                    = customAlertConfigs.flatMap(_.customAlerts)
    val currentEnvironment: Environment = Environment.get(environment)
    val activeAlerts: Seq[CustomAlert]  = filterDisabledAlerts(customAlerts, currentEnvironment)

    val customLogAlerts: Seq[CustomLogAlert] = activeAlerts
      .collect { case alert: CustomLogAlert =>
        alert
      }
      .map { alert =>
        alert.copy(thresholds = alert.thresholds.removeAllOtherEnvironmentThresholds(currentEnvironment))
      }

    val customGraphiteMetricAlert: Seq[CustomGraphiteMetricAlert] = activeAlerts
      .collect { case alert: CustomGraphiteMetricAlert =>
        alert
      }
      .map { alert =>
        alert.copy(thresholds = alert.thresholds.removeAllOtherEnvironmentThresholds(currentEnvironment))
      }

    val customCloudWatchMetricAlerts: Seq[CustomCloudWatchMetricAlert] = activeAlerts
      .collect { case alert: CustomCloudWatchMetricAlert =>
        alert
      }
      .map { alert =>
        alert.copy(thresholds = alert.thresholds.removeAllOtherEnvironmentThresholds(currentEnvironment))
      }

    val separatedAlerts = CustomAlertsTopLevel(CustomAlerts(customLogAlerts, customGraphiteMetricAlert, customCloudWatchMetricAlerts))

    mapper.writeValue(saveLocation, separatedAlerts)
  }

  /** @param customAlerts
    *   Custom Alerts to filter
    * @param currentEnvironment
    *   Environment we're generating YAML for
    * @return
    *   customAlerts list passed in minus any alerts that don't have a threshold defined for this environment
    */
  private def filterDisabledAlerts(customAlerts: Seq[CustomAlert], currentEnvironment: Environment): Seq[CustomAlert] = {
    customAlerts.filter(_.thresholds.isEnvironmentDefined(currentEnvironment))
  }

}
