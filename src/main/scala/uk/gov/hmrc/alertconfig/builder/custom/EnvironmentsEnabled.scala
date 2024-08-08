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

/** Define which environments this alert should be active in
  *
  * @param development
  *   Set to true if alert should be active in the Development environment.
  * @param externaltest
  *   Set to true if alert should be active in the External Test environment.
  * @param integration
  *   Set to true if alert should be active in the Integration environment.
  * @param management
  *   Set to true if alert should be active in the Management environment.
  * @param production
  *   Set to true if alert should be active in the Production environment.
  * @param qa
  *   Set to true if alert should be active in the QA environment.
  * @param staging
  *   Set to true if alert should be active in the Staging environment.
  */
case class EnvironmentsEnabled(
    development: Boolean = false,
    externaltest: Boolean = false,
    integration: Boolean = false,
    management: Boolean = false,
    production: Boolean = false,
    qa: Boolean = false,
    staging: Boolean = false
) {

  /** Checks if the alert is enabled for the given environment
    *
    * @param environment
    *   The environment to check.
    * @return
    *   True if the alert should be active in the given environment otherwise false.
    */
  def isEnvironmentDefined(environment: Environment): Boolean = {
    environment match {
      case Environment.Development  => development
      case Environment.ExternalTest => externaltest
      case Environment.Integration  => integration
      case Environment.Management   => management
      case Environment.Production   => production
      case Environment.Qa           => qa
      case Environment.Staging      => staging
    }
  }

}
