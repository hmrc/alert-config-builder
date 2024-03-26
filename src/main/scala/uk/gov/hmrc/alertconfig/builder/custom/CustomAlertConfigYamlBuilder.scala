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

case class CustomAlerts(customLogAlerts: Seq[CustomLogAlert], customMetricAlerts: Seq[CustomMetricAlert])

object CustomAlertConfigYamlBuilder {
  def run(customAlertConfigs: Seq[CustomAlertConfig], environment: String): Unit = {

    val customAlerts = customAlertConfigs.flatMap(_.customAlerts)
    val currentEnvironment: Environment = Environment.get(environment)
    val activeAlerts: Seq[CustomAlert] = filterDisabledAlerts(customAlerts, currentEnvironment)

    val customLogAlerts: Seq[CustomLogAlert] = activeAlerts.collect {
      case alert: CustomLogAlert => alert
    }.map { alert =>
      alert.copy(thresholds = filterOtherEnvironmentThresholds(alert.thresholds, currentEnvironment))
    }

    val customMetricAlerts: Seq[CustomMetricAlert] = activeAlerts.collect {
      case alert: CustomMetricAlert => alert
    }.map { alert =>
      alert.copy(thresholds = filterOtherEnvironmentThresholds(alert.thresholds, currentEnvironment))
    }

    val separatedAlerts = CustomAlertsTopLevel(CustomAlerts(customLogAlerts, customMetricAlerts))

    mapper.writeValue(new File(s"./target/output/custom-alerts.yml"), separatedAlerts)
  }

  /**
   * @param customAlerts Custom Alerts list to filter
   * @param currentEnvironment Environment we're generating YAML for
   * @return customAlerts list passed in minus any alerts that don't have a threshold defined for this enviornment
   */
  def filterDisabledAlerts(customAlerts: Seq[CustomAlert], currentEnvironment: Environment): Seq[CustomAlert] = {
    customAlerts.filter(_.thresholds.isEnvironmentDefined(currentEnvironment))
  }

  /**
   * Filters out the threshold values for any other environments to allow a cleaner YAML file later
   * Assumes that the threshold objects being passed in have the currentEnvironment defined
   *
   * @param thresholds         Thresholds for a specific alert
   * @param currentEnvironment The environment that the YAML is being generated for
   * @return Object containing just the threshold for the current environment
   */
  def filterOtherEnvironmentThresholds(thresholds: EnvironmentThresholds, currentEnvironment: Environment): EnvironmentThresholds = {
    thresholds.getThresholdForEnvironment(currentEnvironment)
  }

//  def collectCustomAlertsByType[A <: CustomAlert](customAlerts: Seq[CustomAlert], currentEnvironment: Environment): Seq[A] = {
//    customAlerts.collect {
//      case alert: A => alert
//    }.map { alert =>
//      alert.copy(thresholds = filterOtherEnvironmentThresholds(alert.thresholds, currentEnvironment))
//    }
//  }

}
