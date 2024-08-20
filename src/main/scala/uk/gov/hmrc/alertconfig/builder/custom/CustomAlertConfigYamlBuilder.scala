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
    customElasticsearchAlerts: Seq[CustomElasticsearchAlert],
    customGraphiteMetricAlerts: Seq[CustomGraphiteMetricAlert],
    customCloudWatchMetricAlerts: Seq[CustomCloudWatchMetricAlert]
)

object CustomAlertConfigYamlBuilder {

  def run(customAlertConfigs: Seq[CustomAlertConfig], environment: String, saveLocation: File): Unit = {

    val currentEnvironment: Environment = Environment.get(environment)
    val activeAlerts: Seq[CustomAlert]  = filterDisabledAlerts(customAlertConfigs, currentEnvironment)

    val customElasticsearchAlerts: Seq[CustomElasticsearchAlert] = activeAlerts
      .collect { case alert: CustomElasticsearchAlert =>
        alert.copy(thresholds = alert.thresholds.removeAllOtherEnvironmentThresholds(currentEnvironment))
      }

    val customGraphiteMetricAlerts: Seq[CustomGraphiteMetricAlert] = activeAlerts
      .collect { case alert: CustomGraphiteMetricAlert =>
        alert.copy(thresholds = alert.thresholds.removeAllOtherEnvironmentThresholds(currentEnvironment))
      }

    val customCloudWatchMetricAlerts: Seq[CustomCloudWatchMetricAlert] = activeAlerts
      .collect { case alert: CustomCloudWatchMetricAlert =>
        alert.copy(thresholds = alert.thresholds.removeAllOtherEnvironmentThresholds(currentEnvironment))
      }

    val separatedAlerts = CustomAlertsTopLevel(CustomAlerts(customElasticsearchAlerts, customGraphiteMetricAlerts, customCloudWatchMetricAlerts))

    mapper.writeValue(saveLocation, separatedAlerts)
  }

  /** Checks every single [[CustomAlert]] and removes any that do not have a threshold defined for the current environment It also updates any
    * remaining alerts by removing any integrations that do not have an associated EnvironmentAlertBuilder with the current environment set to enabled
    * @param customAlertConfigs
    *   All of the custom alert configs
    * @param currentEnvironment
    *   Environment the YAML is being generated for
    * @return
    */
  private def filterDisabledAlerts(customAlertConfigs: Seq[CustomAlertConfig], currentEnvironment: Environment): Seq[CustomAlert] = {
    customAlertConfigs.flatMap { customAlertConfig =>
      customAlertConfig.customAlerts
        .filter { customAlert =>
          isAlertDefinedForEnv(customAlert, currentEnvironment)
        }
        .flatMap { customAlert =>
          val enabledIntegrationsForAlert = customAlert.integrations.filter(isIntegrationEnabledForEnv(_, customAlertConfig, currentEnvironment))
          Option.when(enabledIntegrationsForAlert.nonEmpty)(updateIntegrationsForCustomAlert(customAlert, enabledIntegrationsForAlert))
        }
    }
  }

  /** Checks the [[CustomAlert]] to see if an alert threshold has been configured for the current environment.
    *
    * @param alert
    *   Alert to check
    * @param currentEnvironment
    *   Environment the YAML is being generated for
    * @return
    */
  private def isAlertDefinedForEnv(alert: CustomAlert, currentEnvironment: Environment): Boolean = {
    alert match {
      case alert: CustomGraphiteMetricAlert   => alert.thresholds.isEnvironmentDefined(currentEnvironment)
      case alert: CustomElasticsearchAlert    => alert.thresholds.isEnvironmentDefined(currentEnvironment)
      case alert: CustomCloudWatchMetricAlert => alert.thresholds.isEnvironmentDefined(currentEnvironment)
      case other => throw new IllegalArgumentException(s"isAlertDefinedForEnv is not defined for ${other.getClass.getSimpleName}. Please update it.")
    }
  }

  /** For the given integration checks if there is an [[uk.gov.hmrc.alertconfig.builder.EnvironmentAlertBuilder]] defined as well as checking that the
    * current environment is enabled for the given integration
    *
    * @param integration
    *   PagerDuty integration to check
    * @param customAlertConfig
    *   Config that contains the [[uk.gov.hmrc.alertconfig.builder.EnvironmentAlertBuilder]]s for this set of alerts
    * @param currentEnvironment
    *   Environment the YAML is being generated for
    * @return
    */
  private def isIntegrationEnabledForEnv(integration: String, customAlertConfig: CustomAlertConfig, currentEnvironment: Environment): Boolean = {
    customAlertConfig.environmentConfig.find(_.integrationName == integration).exists { environmentAlertBuilder =>
      environmentAlertBuilder.enabledEnvironments.contains(currentEnvironment)
    }
  }

  /** Takes a [[CustomAlert]] and a Seq of integrations that are enabled in the environment and updates it based on the type
    * @param customAlert
    *   The custom alert to update the integrations in
    * @param enabledIntegrations
    *   The enabled PagerDuty integrations for this alert to be used to replace the existing ones
    * @return
    */
  private def updateIntegrationsForCustomAlert(customAlert: CustomAlert, enabledIntegrations: Seq[String]): CustomAlert = {
    customAlert match {
      case alert: CustomCloudWatchMetricAlert => alert.copy(integrations = enabledIntegrations)
      case alert: CustomElasticsearchAlert    => alert.copy(integrations = enabledIntegrations)
      case alert: CustomGraphiteMetricAlert   => alert.copy(integrations = enabledIntegrations)
      case _                                  => throw new IllegalArgumentException(s"Unsupported CustomAlert type: ${customAlert.getClass.getName}")
    }
  }

}
