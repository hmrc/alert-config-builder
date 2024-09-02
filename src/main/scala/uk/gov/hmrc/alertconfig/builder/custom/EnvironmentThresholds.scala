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

/** Define thresholds for any environments you want this custom alert to be active in.
  *
  * @param development
  *   The threshold for the development environment.
  * @param externaltest
  *   The threshold for the external test environment.
  * @param integration
  *   The threshold for the integration environment.
  * @param management
  *   The threshold for the management environment.
  * @param production
  *   The threshold for the production environment.
  * @param qa
  *   The threshold for the quality assurance environment.
  * @param staging
  *   The threshold for the staging environment.
  */
case class EnvironmentThresholds(
    development: Option[Long] = None,
    externaltest: Option[Long] = None,
    integration: Option[Long] = None,
    management: Option[Long] = None,
    production: Option[Long] = None,
    qa: Option[Long] = None,
    staging: Option[Long] = None
) {

  /** Checks if the given environment has a threshold defined.
    *
    * @param environment
    *   The environment to check.
    * @return
    *   True if the threshold for the given environment is defined, otherwise false.
    */
  def isEnvironmentDefined(environment: Environment): Boolean = {
    environment match {
      case Environment.Development  => development.isDefined
      case Environment.ExternalTest => externaltest.isDefined
      case Environment.Integration  => integration.isDefined
      case Environment.Management   => management.isDefined
      case Environment.Production   => production.isDefined
      case Environment.Qa           => qa.isDefined
      case Environment.Staging      => staging.isDefined
    }
  }

  /** Removes the thresholds for all environments other than the one requested.
    *
    * @param environment
    *   The environment to keep the threshold for.
    * @return
    *   EnvironmentThresholds with thresholds for only the specified environment.
    */
  def removeAllOtherEnvironmentThresholds(environment: Environment): EnvironmentThresholds = {
    environment match {
      case Environment.Development  => EnvironmentThresholds(development = development)
      case Environment.ExternalTest => EnvironmentThresholds(externaltest = externaltest)
      case Environment.Integration  => EnvironmentThresholds(integration = integration)
      case Environment.Management   => EnvironmentThresholds(management = management)
      case Environment.Production   => EnvironmentThresholds(production = production)
      case Environment.Qa           => EnvironmentThresholds(qa = qa)
      case Environment.Staging      => EnvironmentThresholds(staging = staging)
    }
  }

}

/** Set common threshold for all environments
  */
object EnvironmentThresholds {

  /** Creates EnvironmentThresholds with the same threshold for all environments.
    *
    * @param threshold
    *   An integer to be set as the threshold for all six environments.
    * @return
    *   EnvironmentThresholds with the same threshold for all environments.
    */
  def forAllEnvironments(threshold: Int): EnvironmentThresholds = EnvironmentThresholds(
    production = Some(threshold),
    externaltest = Some(threshold),
    staging = Some(threshold),
    qa = Some(threshold),
    development = Some(threshold),
    integration = Some(threshold)
  )

  /** Creates EnvironmentThresholds with the same threshold for production and external test environments.
    *
    * @param threshold
    *   An integer to be set for the production and external test environments.
    * @return
    *   EnvironmentThresholds with the same threshold for production and external test environments.
    */
  def forAllProdEnvironments(threshold: Int): EnvironmentThresholds = EnvironmentThresholds(
    production = Some(threshold),
    externaltest = Some(threshold)
  )

  /** Creates EnvironmentThresholds with the same threshold for all non-production environments.
    *
    * @param threshold
    *   An integer to be set as the threshold for all non-production environments.
    * @return
    *   EnvironmentThresholds with the same threshold for all non-production environments.
    */
  def forAllNonProdEnvironments(threshold: Int): EnvironmentThresholds = EnvironmentThresholds(
    staging = Some(threshold),
    qa = Some(threshold),
    development = Some(threshold),
    integration = Some(threshold)
  )

  /** Creates EnvironmentThresholds with the same threshold for all environments including management.
   *
   * @param threshold
   * An integer to be set as the threshold for all seven environments including management.
   * @return
   * EnvironmentThresholds with the same threshold for all environments.
   */
  def forAllEnvironmentsPlusManagement(threshold: Int): EnvironmentThresholds = EnvironmentThresholds(
    production = Some(threshold),
    externaltest = Some(threshold),
    staging = Some(threshold),
    qa = Some(threshold),
    development = Some(threshold),
    integration = Some(threshold),
    management = Some(threshold)
  )

}
