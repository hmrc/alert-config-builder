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

import spray.json.{JsArray, JsObject, JsString, JsValue}

sealed trait Severity {
  override def toString: String = this.getClass.getSimpleName.toLowerCase.replace("$", "")
}

object Severity {
  object Info     extends Severity
  object Ok       extends Severity
  object Warning  extends Severity
  object Critical extends Severity
  object Unknown  extends Severity
}

sealed trait Environment {
  override def toString: String = s"aws_${this.getClass.getSimpleName.toLowerCase.replace("$", "")}"
}

object Environment {
  object Integration  extends Environment
  object Development  extends Environment
  object Qa           extends Environment
  object Staging      extends Environment
  object ExternalTest extends Environment
  object Management   extends Environment
  object Production   extends Environment

  def get(env: String): Environment = {
    env.toLowerCase.trim.replaceAll(" ", "") match {
      case "integration"  => Integration
      case "development"  => Development
      case "qa"           => Qa
      case "staging"      => Staging
      case "externaltest" => ExternalTest
      case "management"   => Management
      case "production"   => Production
    }
  }

}

object AllEnvironmentAlertConfigBuilder {

  def build(builders: Iterable[EnvironmentAlertBuilder]): Map[Environment, JsObject] =
    Seq(
      Environment.Integration,
      Environment.Development,
      Environment.Qa,
      Environment.Staging,
      Environment.ExternalTest,
      Environment.Management,
      Environment.Production
    )
      .map(e => e -> JsObject("handlers" -> JsObject(builders.map(_.alertConfigFor(e)).toList.sortBy(_._1): _*)))
      .toMap

}

case class EnvironmentAlertBuilder(
    integrationName: String,
    command: Option[JsValue] = None,
    enabledEnvironments: Map[Environment, Set[Severity]] = Map((Environment.Production, Set(Severity.Ok, Severity.Warning, Severity.Critical))),
    customEnvironmentNames: Map[Environment, String] = Map((Environment.Production, "aws_production")),
    integrationFilters: Map[Environment, JsValue] = Map((Environment.Production, JsString("occurrences")))
) {

  private val defaultSeverities: Set[Severity] = Set(Severity.Ok, Severity.Warning, Severity.Critical)
  private val defaultFilter: JsValue           = JsString("occurrences")
  private val defaultMgmtFilter: JsValue       = JsArray(JsString("occurrences"), JsString("kitchen_filter"), JsString("packer_filter"))

  def inIntegration(
      severities: Set[Severity] = defaultSeverities,
      customEnv: String = Environment.Integration.toString,
      customFilter: JsValue = defaultFilter
  ): EnvironmentAlertBuilder =
    this.copy(
      enabledEnvironments = enabledEnvironments + (Environment.Integration       -> severities),
      customEnvironmentNames = customEnvironmentNames + (Environment.Integration -> customEnv),
      integrationFilters = integrationFilters + (Environment.Integration         -> customFilter)
    )

  def inDevelopment(
      severities: Set[Severity] = defaultSeverities,
      customEnv: String = Environment.Development.toString,
      customFilter: JsValue = defaultFilter
  ): EnvironmentAlertBuilder =
    this.copy(
      enabledEnvironments = enabledEnvironments + (Environment.Development       -> severities),
      customEnvironmentNames = customEnvironmentNames + (Environment.Development -> customEnv),
      integrationFilters = integrationFilters + (Environment.Development         -> customFilter)
    )

  def inQa(
      severities: Set[Severity] = defaultSeverities,
      customEnv: String = Environment.Qa.toString,
      customFilter: JsValue = defaultFilter
  ): EnvironmentAlertBuilder =
    this.copy(
      enabledEnvironments = enabledEnvironments + (Environment.Qa       -> severities),
      customEnvironmentNames = customEnvironmentNames + (Environment.Qa -> customEnv),
      integrationFilters = integrationFilters + (Environment.Qa         -> customFilter)
    )

  def inStaging(
      severities: Set[Severity] = defaultSeverities,
      customEnv: String = Environment.Staging.toString,
      customFilter: JsValue = defaultFilter
  ): EnvironmentAlertBuilder =
    this.copy(
      enabledEnvironments = enabledEnvironments + (Environment.Staging       -> severities),
      customEnvironmentNames = customEnvironmentNames + (Environment.Staging -> customEnv),
      integrationFilters = integrationFilters + (Environment.Staging         -> customFilter)
    )

  def inExternalTest(
      severities: Set[Severity] = defaultSeverities,
      customEnv: String = Environment.ExternalTest.toString,
      customFilter: JsValue = defaultFilter
  ): EnvironmentAlertBuilder =
    this.copy(
      enabledEnvironments = enabledEnvironments + (Environment.ExternalTest       -> severities),
      customEnvironmentNames = customEnvironmentNames + (Environment.ExternalTest -> customEnv),
      integrationFilters = integrationFilters + (Environment.ExternalTest         -> customFilter)
    )

  def inManagement(
      severities: Set[Severity] = defaultSeverities,
      customEnv: String = Environment.Management.toString,
      customFilter: JsValue = defaultMgmtFilter
  ): EnvironmentAlertBuilder =
    this.copy(
      enabledEnvironments = enabledEnvironments + (Environment.Management       -> severities),
      customEnvironmentNames = customEnvironmentNames + (Environment.Management -> customEnv),
      integrationFilters = integrationFilters + (Environment.Management         -> customFilter)
    )

  def inProduction(
      severities: Set[Severity] = defaultSeverities,
      customEnv: String = Environment.Production.toString,
      customFilter: JsValue = defaultFilter
  ): EnvironmentAlertBuilder =
    this.copy(
      enabledEnvironments = enabledEnvironments + (Environment.Production       -> severities),
      customEnvironmentNames = customEnvironmentNames + (Environment.Production -> customEnv),
      integrationFilters = integrationFilters + (Environment.Production         -> customFilter)
    )

  def disableProduction(): EnvironmentAlertBuilder =
    this.copy(enabledEnvironments = enabledEnvironments - Environment.Production)

  def withCommand(customCommand: String): EnvironmentAlertBuilder =
    this.copy(command = Option(JsString(customCommand)))

  def alertConfigFor(environment: Environment): (String, JsObject) = {
    val filterType: String =
      if (integrationFilters.getOrElse(environment, defaultFilter).isInstanceOf[JsArray])
        "filters"
      else
        "filter"

    integrationName ->
      JsObject(
        "command"      -> commandFor(integrationName, environment),
        "type"         -> JsString("pipe"),
        "severities"   -> severitiesFor(environment),
        s"$filterType" -> integrationFilters.getOrElse(environment, defaultFilter)
      )
  }

  private def commandFor(service: String, environment: Environment): JsValue =
    if (enabledEnvironments.contains(environment))
      command.getOrElse(
        JsString(s"/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team $service -e ${customEnvironmentNames(environment)}"))
    else
      JsString("/etc/sensu/handlers/noop.rb")

  private def severitiesFor(environment: Environment) =
    JsArray(
      enabledEnvironments
        .getOrElse(environment, defaultSeverities)
        // Temporary filter as part of TEL-4408 to ensure that info level
        // notifications can't be enabled. They are only added to specific YAML
        // files. Yes this is a hack, and when Sensu is gone, it can go too!
        .filter(s => !s.toString.equalsIgnoreCase("info"))
        .map(s => JsString(s.toString))
        .toVector)

}
