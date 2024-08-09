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

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json._

class EnvironmentAlertBuilderSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  "EnvironmentAlertBuilder" should {
    "create config with production enabled" in {
      EnvironmentAlertBuilder("team-telemetry").inProduction().alertConfigFor(Environment.Production) shouldBe
        "team-telemetry" ->
        JsObject(
          "command"    -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team team-telemetry -e aws_production"),
          "type"       -> JsString("pipe"),
          "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
          "filter"     -> JsString("occurrences")
        )
    }

    "create config without production enabled" in {
      EnvironmentAlertBuilder("team-telemetry").alertConfigFor(Environment.Production) shouldBe
        "team-telemetry" ->
        JsObject(
          "command"    -> JsString("/etc/sensu/handlers/noop.rb"),
          "type"       -> JsString("pipe"),
          "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
          "filter"     -> JsString("occurrences")
        )
    }

    "create config with integration enabled with default severities" in {
      EnvironmentAlertBuilder("infra").inIntegration().alertConfigFor(Environment.Integration) shouldBe
        "infra" ->
        JsObject(
          "command"    -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team infra -e aws_integration"),
          "type"       -> JsString("pipe"),
          "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
          "filter"     -> JsString("occurrences")
        )
    }

    "create config with integration enabled with custom command" in {
      EnvironmentAlertBuilder("infra")
        .withCommand("/etc/sensu/handlers/some-custom-ruby-handler.rb")
        .inIntegration()
        .alertConfigFor(Environment.Integration) shouldBe
        "infra" ->
        JsObject(
          "command"    -> JsString("/etc/sensu/handlers/some-custom-ruby-handler.rb"),
          "type"       -> JsString("pipe"),
          "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
          "filter"     -> JsString("occurrences")
        )
    }

    "create config with integration disabled with custom command" in {
      EnvironmentAlertBuilder("infra").withCommand("/etc/sensu/handlers/dose-pagerduty-high.rb").alertConfigFor(Environment.Integration) shouldBe
        "infra" ->
        JsObject(
          "command"    -> JsString("/etc/sensu/handlers/noop.rb"),
          "type"       -> JsString("pipe"),
          "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
          "filter"     -> JsString("occurrences")
        )
    }

    "create config for txm-infra with integration enabled and custom environment" in {
      EnvironmentAlertBuilder("txm-infra").inIntegration(customEnv = "txm_integration").alertConfigFor(Environment.Integration) shouldBe
        "txm-infra" ->
        JsObject(
          "command"    -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team txm-infra -e txm_integration"),
          "type"       -> JsString("pipe"),
          "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
          "filter"     -> JsString("occurrences")
        )
    }

    "create config with integration disabled" in {
      EnvironmentAlertBuilder("labs-team-telemetry").alertConfigFor(Environment.Integration) shouldBe
        "labs-team-telemetry" ->
        JsObject(
          "command"    -> JsString("/etc/sensu/handlers/noop.rb"),
          "type"       -> JsString("pipe"),
          "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
          "filter"     -> JsString("occurrences")
        )
    }

    "create config with integration enabled with custom severities ignores info" in {
      EnvironmentAlertBuilder("team-telemetry")
        .inIntegration(Set(Severity.Ok, Severity.Info, Severity.Warning, Severity.Critical, Severity.Unknown))
        .alertConfigFor(Environment.Integration) shouldBe
        "team-telemetry" ->
        JsObject(
          "command"    -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team team-telemetry -e aws_integration"),
          "type"       -> JsString("pipe"),
          "severities" -> JsArray(JsString("critical"), JsString("unknown"), JsString("warning"), JsString("ok")),
          "filter"     -> JsString("occurrences")
        )
    }

    "create config with management enabled should filter kitchen & packer" in {
      EnvironmentAlertBuilder("infra").inManagement().alertConfigFor(Environment.Management) shouldBe
        "infra" ->
        JsObject(
          "command"    -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team infra -e aws_management"),
          "type"       -> JsString("pipe"),
          "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
          "filters"    -> JsArray(JsString("occurrences"), JsString("kitchen_filter"), JsString("packer_filter"))
        )
    }

    "create config with development enabled with custom severities" in {
      EnvironmentAlertBuilder("team-telemetry")
        .inDevelopment(Set(Severity.Ok, Severity.Warning, Severity.Critical, Severity.Unknown))
        .alertConfigFor(Environment.Development) shouldBe
        "team-telemetry" ->
        JsObject(
          "command"    -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team team-telemetry -e aws_development"),
          "type"       -> JsString("pipe"),
          "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical"), JsString("unknown")),
          "filter"     -> JsString("occurrences")
        )
    }

    "create config with qa enabled with custom severities" in {
      EnvironmentAlertBuilder("team-telemetry")
        .inQa(Set(Severity.Ok, Severity.Warning, Severity.Critical, Severity.Unknown))
        .alertConfigFor(Environment.Qa) shouldBe
        "team-telemetry" ->
        JsObject(
          "command"    -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team team-telemetry -e aws_qa"),
          "type"       -> JsString("pipe"),
          "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical"), JsString("unknown")),
          "filter"     -> JsString("occurrences")
        )
    }

    "create config with staging enabled with custom severities" in {
      EnvironmentAlertBuilder("team-telemetry")
        .inStaging(Set(Severity.Ok, Severity.Warning, Severity.Critical, Severity.Unknown))
        .alertConfigFor(Environment.Staging) shouldBe
        "team-telemetry" ->
        JsObject(
          "command"    -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team team-telemetry -e aws_staging"),
          "type"       -> JsString("pipe"),
          "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical"), JsString("unknown")),
          "filter"     -> JsString("occurrences")
        )
    }

    "create config with external test enabled with custom severities" in {
      EnvironmentAlertBuilder("team-telemetry")
        .inExternalTest(Set(Severity.Ok, Severity.Warning, Severity.Critical, Severity.Unknown))
        .alertConfigFor(Environment.ExternalTest) shouldBe
        "team-telemetry" ->
        JsObject(
          "command"    -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team team-telemetry -e aws_externaltest"),
          "type"       -> JsString("pipe"),
          "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical"), JsString("unknown")),
          "filter"     -> JsString("occurrences")
        )
    }

    "create config with production enabled with custom severities" in {
      EnvironmentAlertBuilder("team-telemetry")
        .inProduction(Set(Severity.Ok, Severity.Warning, Severity.Critical, Severity.Unknown))
        .alertConfigFor(Environment.Production) shouldBe
        "team-telemetry" ->
        JsObject(
          "command"    -> JsString("/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team team-telemetry -e aws_production"),
          "type"       -> JsString("pipe"),
          "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical"), JsString("unknown")),
          "filter"     -> JsString("occurrences")
        )
    }
  }

  "Environment" when {
    "using the get function" should {
      "return specified environment" in {
        Environment.get("integration") shouldBe Environment.Integration
        Environment.get("development") shouldBe Environment.Development
        Environment.get("qa") shouldBe Environment.Qa
        Environment.get("staging") shouldBe Environment.Staging
        Environment.get("externaltest") shouldBe Environment.ExternalTest
        Environment.get("management") shouldBe Environment.Management
        Environment.get("production") shouldBe Environment.Production
      }
      "work ignoring case" in {
        Environment.get("PrOdUcTiOn") shouldBe Environment.Production
      }
      "work ignoring whitespace" in {
        Environment.get("external test") shouldBe Environment.ExternalTest
      }
      "work ignoring any padded whitespace" in {
        Environment.get("  production  ") shouldBe Environment.Production
      }
    }
  }

}
