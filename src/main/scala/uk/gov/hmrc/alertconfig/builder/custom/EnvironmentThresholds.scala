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

/**
 * Define thresholds for any environments you want this custom
 * alert to be active in.
 */
case class EnvironmentThresholds(
                                  development: Option[Int] = None,
                                  externaltest: Option[Int] = None,
                                  integration: Option[Int] = None,
                                  management: Option[Int] = None,
                                  production: Option[Int] = None,
                                  qa: Option[Int] = None,
                                  staging: Option[Int] = None
                                ) {
  /**
   * Checks if the given environment has a threshold defined.
   */
  def isEnvironmentDefined(environment: Environment): Boolean = {
    environment match {
      case Environment.Development => development.isDefined
      case Environment.ExternalTest => externaltest.isDefined
      case Environment.Integration => integration.isDefined
      case Environment.Management => management.isDefined
      case Environment.Production => production.isDefined
      case Environment.Qa => qa.isDefined
      case Environment.Staging => staging.isDefined
    }
  }

  /**
   * Removes the thresholds for all environments other than the one requested.
   */
  def removeAllOtherEnvironmentThresholds(environment: Environment): EnvironmentThresholds = {
    environment match {
      case Environment.Development => EnvironmentThresholds(development = development)
      case Environment.ExternalTest => EnvironmentThresholds(externaltest = externaltest)
      case Environment.Integration => EnvironmentThresholds(integration = integration)
      case Environment.Management => EnvironmentThresholds(management = management)
      case Environment.Production => EnvironmentThresholds(production = production)
      case Environment.Qa => EnvironmentThresholds(qa = qa)
      case Environment.Staging => EnvironmentThresholds(staging = staging)
    }
  }

}
