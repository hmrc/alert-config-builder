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

import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import spray.json._

class EnvironmentAlertBuilderSpec  extends WordSpec with Matchers with BeforeAndAfterEach {

  "EnvironmentAlertBuilder" should {
    "create config with production enabled by default" in {
      EnvironmentAlertBuilder("team-telemetry").alertConfigFor(Production) shouldBe
        "team-telemetry" ->
          JsObject(
            "command" -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team team-telemetry -e aws_production"),
            "type" -> JsString("pipe"),
            "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
            "filter" -> JsString("occurrences"))
    }

    "create config with production disabled" in {
      EnvironmentAlertBuilder("team-telemetry").disableProduction().alertConfigFor(Production) shouldBe
        "team-telemetry" ->
          JsObject(
            "command" -> JsString("/etc/sensu/handlers/noop.rb"),
            "type" -> JsString("pipe"),
            "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
            "filter" -> JsString("occurrences"))
    }

    "create config with integration enabled with default severities" in {
      EnvironmentAlertBuilder("infra").inIntegration().alertConfigFor(Integration) shouldBe
        "infra" ->
          JsObject(
            "command" -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team infra -e aws_integration"),
            "type" -> JsString("pipe"),
            "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
            "filter" -> JsString("occurrences"))
    }

    "create config with integration enabled with custom command" in {
      EnvironmentAlertBuilder("infra").withCommand("/etc/sensu/handlers/dose-pagerduty-high.rb").inIntegration().alertConfigFor(Integration) shouldBe
        "infra" ->
          JsObject(
            "command" -> JsString("/etc/sensu/handlers/dose-pagerduty-high.rb"),
            "type" -> JsString("pipe"),
            "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
            "filter" -> JsString("occurrences"))
    }

    "create config with integration disabled with custom command" in {
      EnvironmentAlertBuilder("infra").withCommand("/etc/sensu/handlers/dose-pagerduty-high.rb").alertConfigFor(Integration) shouldBe
        "infra" ->
          JsObject(
            "command" -> JsString("/etc/sensu/handlers/noop.rb"),
            "type" -> JsString("pipe"),
            "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
            "filter" -> JsString("occurrences"))
    }

    "create config for txm-infra with integration enabled and custom environment" in {
      EnvironmentAlertBuilder("txm-infra").inIntegration(customEnv = "txm_integration").alertConfigFor(Integration) shouldBe
        "txm-infra" ->
          JsObject(
            "command" -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team txm-infra -e txm_integration"),
            "type" -> JsString("pipe"),
            "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
            "filter" -> JsString("occurrences"))
    }

    "create config with integration disabled" in {
      EnvironmentAlertBuilder("labs-team-telemetry").alertConfigFor(Integration) shouldBe
        "labs-team-telemetry" ->
          JsObject(
            "command" -> JsString("/etc/sensu/handlers/noop.rb"),
            "type" -> JsString("pipe"),
            "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
            "filter" -> JsString("occurrences"))
    }

    "create config with integration enabled with custom severities" in {
      EnvironmentAlertBuilder("team-telemetry").inIntegration(Set(Ok, Warning, Critical, Unknown)).alertConfigFor(Integration) shouldBe
        "team-telemetry" ->
          JsObject(
            "command" -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team team-telemetry -e aws_integration"),
            "type" -> JsString("pipe"),
            "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical"), JsString("unknown")),
            "filter" -> JsString("occurrences"))
    }

    "create config with management enabled should filter kitchen & packer" in {
      EnvironmentAlertBuilder("infra").inManagement().alertConfigFor(Management) shouldBe
        "infra" ->
          JsObject(
            "command" -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team infra -e aws_management"),
            "type" -> JsString("pipe"),
            "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
            "filters" -> JsArray(JsString("occurrences"), JsString("kitchen_filter"), JsString("packer_filter")))
    }


    "create config with development enabled with custom severities" in {
      EnvironmentAlertBuilder("team-telemetry").inDevelopment(Set(Ok, Warning, Critical, Unknown)).alertConfigFor(Development) shouldBe
        "team-telemetry" ->
          JsObject(
            "command" -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team team-telemetry -e aws_development"),
            "type" -> JsString("pipe"),
            "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical"), JsString("unknown")),
            "filter" -> JsString("occurrences"))
    }

    "create config with qa enabled with custom severities" in {
      EnvironmentAlertBuilder("team-telemetry").inQa(Set(Ok, Warning, Critical, Unknown)).alertConfigFor(Qa) shouldBe
        "team-telemetry" ->
          JsObject(
            "command" -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team team-telemetry -e aws_qa"),
            "type" -> JsString("pipe"),
            "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical"), JsString("unknown")),
            "filter" -> JsString("occurrences"))
    }

    "create config with staging enabled with custom severities" in {
      EnvironmentAlertBuilder("team-telemetry").inStaging(Set(Ok, Warning, Critical, Unknown)).alertConfigFor(Staging) shouldBe
        "team-telemetry" ->
          JsObject(
            "command" -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team team-telemetry -e aws_staging"),
            "type" -> JsString("pipe"),
            "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical"), JsString("unknown")),
            "filter" -> JsString("occurrences"))
    }

    "create config with external test enabled with custom severities" in {
      EnvironmentAlertBuilder("team-telemetry").inExternalTest(Set(Ok, Warning, Critical, Unknown)).alertConfigFor(ExternalTest) shouldBe
        "team-telemetry" ->
          JsObject(
            "command" -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team team-telemetry -e aws_externaltest"),
            "type" -> JsString("pipe"),
            "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical"), JsString("unknown")),
            "filter" -> JsString("occurrences"))
    }

    "create config with production enabled with custom severities" in {
      EnvironmentAlertBuilder("team-telemetry").inProduction(Set(Ok, Warning, Critical, Unknown)).alertConfigFor(Production) shouldBe
        "team-telemetry" ->
          JsObject(
            "command" -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team team-telemetry -e aws_production"),
            "type" -> JsString("pipe"),
            "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical"), JsString("unknown")),
            "filter" -> JsString("occurrences"))
    }
  }
}
