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
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import spray.json.{JsArray, JsObject, JsString}

class AllEnvironmentAlertConfigBuilderSpec extends AnyFunSuite with Matchers with BeforeAndAfterEach {

  def defaultNoopHandlerConfig: JsObject =
    JsObject(
      "command"    -> JsString("/etc/sensu/handlers/noop.rb"),
      "type"       -> JsString("pipe"),
      "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
      "filter"     -> JsString("occurrences")
    )

  def defaultEnabledHandlerConfig(service: String, environment: Environment): JsObject =
    JsObject(
      "command"    -> JsString(s"/etc/sensu/handlers/hmrc_pagerduty_multiteam_env_apiv2.rb --team $service -e $environment"),
      "type"       -> JsString("pipe"),
      "severities" -> JsArray(JsString("ok"), JsString("warning"), JsString("critical")),
      "filter"     -> JsString("occurrences")
    )

  Seq(Environment.Integration, Environment.Development, Environment.Staging, Environment.Qa, Environment.ExternalTest, Environment.Management)
    .foreach { e =>
      test(s"create config for $e") {
        val environmentConfigMap =
          AllEnvironmentAlertConfigBuilder.build(Set(EnvironmentAlertBuilder("team-telemetry"), EnvironmentAlertBuilder("infra")))

        environmentConfigMap(e) shouldBe
          JsObject(
            "handlers" -> JsObject(
              "infra"          -> defaultNoopHandlerConfig,
              "team-telemetry" -> defaultNoopHandlerConfig
            ))
      }
    }

  test("create config for production") {
    val environmentConfigMap =
      AllEnvironmentAlertConfigBuilder.build(Set(EnvironmentAlertBuilder("team-telemetry"), EnvironmentAlertBuilder("infra")))

    environmentConfigMap(Environment.Production) shouldBe
      JsObject(
        "handlers" -> JsObject(
          "infra"          -> defaultEnabledHandlerConfig("infra", Environment.Production),
          "team-telemetry" -> defaultEnabledHandlerConfig("team-telemetry", Environment.Production)
        ))
  }

}
