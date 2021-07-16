/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.alertconfig.builders

import spray.json.{JsArray, JsNull, JsObject, JsString, JsValue}

sealed trait Severity {
  override def toString: String = this.getClass.getSimpleName.toLowerCase.replace("$", "")
}
object Ok extends Severity
object Warning extends Severity
object Critical extends Severity
object Unknown extends Severity

sealed trait Environment {
  override def toString: String = s"aws_${this.getClass.getSimpleName.toLowerCase.replace("$", "")}"
}
object Integration extends Environment
object Development extends Environment
object Qa extends Environment
object Staging extends Environment
object ExternalTest extends Environment
object Management extends Environment
object Production extends Environment

object AllEnvironmentAlertConfigBuilder {
  def build(builders: Iterable[EnvironmentAlertBuilder]): Map[Environment, JsObject] =
    Seq(Integration, Development, Qa, Staging, ExternalTest, Management, Production).map( e =>
    e -> JsObject("handlers" -> JsObject(builders.map(b => b.alertConfigFor(e)).toList.sortBy(_._1) : _*))).toMap
}

case class EnvironmentAlertBuilder(
                                    handlerName:String,
                                    command:Option[JsValue] = None,
                                    enabledEnvironments: Map[Environment, Set[Severity]] = Map((Production, Set(Ok, Warning, Critical))),
                                    customEnvironmentNames: Map[Environment, String] = Map((Production, "aws_production")),
                                    handlerFilters: Map[Environment, JsValue] = Map((Production, JsString("occurrences")))
                                  ) {

  private val defaultSeverities: Set[Severity] = Set(Ok, Warning, Critical)
  private val defaultFilter: JsValue = JsString("occurrences")
  private val defaultMgmtFilter: JsValue = JsArray(JsString("occurrences"),JsString("kitchen_filter"),JsString("packer_filter"))

  def inIntegration(severities: Set[Severity] = defaultSeverities, customEnv: String = Integration.toString, customFilter: JsValue = defaultFilter): EnvironmentAlertBuilder =
    this.copy(
      enabledEnvironments = enabledEnvironments + (Integration -> severities),
      customEnvironmentNames = customEnvironmentNames + (Integration -> customEnv),
      handlerFilters = handlerFilters + (Integration -> customFilter)
    )
  def inDevelopment(severities: Set[Severity] = defaultSeverities, customEnv: String = Development.toString, customFilter: JsValue = defaultFilter): EnvironmentAlertBuilder =
    this.copy(
      enabledEnvironments = enabledEnvironments + (Development -> severities),
      customEnvironmentNames = customEnvironmentNames + (Development -> customEnv),
      handlerFilters = handlerFilters + (Development -> customFilter)
    )
  def inQa(severities: Set[Severity] = defaultSeverities, customEnv: String = Qa.toString, customFilter: JsValue = defaultFilter): EnvironmentAlertBuilder =
    this.copy(
      enabledEnvironments = enabledEnvironments + (Qa -> severities),
      customEnvironmentNames = customEnvironmentNames + (Qa -> customEnv),
      handlerFilters = handlerFilters + (Qa -> customFilter)
    )
  def inStaging(severities: Set[Severity] = defaultSeverities, customEnv: String = Staging.toString, customFilter: JsValue = defaultFilter): EnvironmentAlertBuilder =
    this.copy(
      enabledEnvironments = enabledEnvironments + (Staging -> severities),
      customEnvironmentNames = customEnvironmentNames + (Staging -> customEnv),
      handlerFilters = handlerFilters + (Staging -> customFilter)
    )
  def inExternalTest(severities: Set[Severity] = defaultSeverities, customEnv: String = ExternalTest.toString, customFilter: JsValue = defaultFilter): EnvironmentAlertBuilder =
    this.copy(
      enabledEnvironments = enabledEnvironments + (ExternalTest -> severities),
      customEnvironmentNames = customEnvironmentNames + (ExternalTest -> customEnv),
      handlerFilters = handlerFilters + (ExternalTest -> customFilter)
    )
  def inManagement(severities: Set[Severity] = defaultSeverities, customEnv: String = Management.toString, customFilter: JsValue = defaultMgmtFilter): EnvironmentAlertBuilder =
    this.copy(
      enabledEnvironments = enabledEnvironments + (Management -> severities),
      customEnvironmentNames = customEnvironmentNames + (Management -> customEnv),
      handlerFilters = handlerFilters + (Management -> customFilter)
    )
  def inProduction(severities: Set[Severity] = defaultSeverities, customEnv: String = Production.toString, customFilter: JsValue = defaultFilter): EnvironmentAlertBuilder =
    this.copy(
      enabledEnvironments = enabledEnvironments + (Production -> severities),
      customEnvironmentNames = customEnvironmentNames + (Production -> customEnv),
      handlerFilters = handlerFilters + (Production -> customFilter)
    )

  def disableProduction(): EnvironmentAlertBuilder =
    this.copy(enabledEnvironments = enabledEnvironments - Production)

  def withCommand(customCommand: String): EnvironmentAlertBuilder =
    this.copy(command = Option(JsString(customCommand)))

  def alertConfigFor(environment: Environment): (String, JsObject) = {
    val filterType: String = {
      if(handlerFilters.getOrElse(environment, defaultFilter).isInstanceOf[JsArray]) "filters"
      else "filter"
    }
    handlerName ->
      JsObject(
        "command" -> commandFor(handlerName, environment),
        "type" -> JsString("pipe"),
        "severities" ->  severitiesFor(environment),
        s"$filterType" -> handlerFilters.getOrElse(environment, defaultFilter))
  }

  private def commandFor(service: String, environment: Environment): JsValue =
    if (enabledEnvironments.contains(environment))
      command.getOrElse(JsString(s"/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team $service -e ${customEnvironmentNames(environment)}"))
    else
      JsString("/etc/sensu/handlers/noop.rb")

  private def severitiesFor(environment: Environment) =
    JsArray(enabledEnvironments.getOrElse(environment, defaultSeverities).map(s => JsString(s.toString)).toVector)

}
