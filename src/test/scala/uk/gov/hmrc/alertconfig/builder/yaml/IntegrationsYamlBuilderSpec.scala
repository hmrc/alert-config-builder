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

package uk.gov.hmrc.alertconfig.builder.yaml

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.alertconfig.builder.{Environment, EnvironmentAlertBuilder, Severity}

import java.io.File
import java.nio.file.Files
import scala.io.Source

class IntegrationsYamlBuilderSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    System.setProperty("app-config-path", "src/test/resources/app-config")
    System.setProperty("zone-mapping-path", "src/test/resources/zone-to-service-domain-mapping.yml")
  }

  "generate" should {
    "create a correctly defined YAML file containing integrations and severities for that env only" in {
      val tmpDir             = Files.createTempDirectory("integrations-test")
      val testFile           = new File(s"$tmpDir/integrations.yaml")
      val currentEnvironment = Environment.Production

      val environmentAlertBuilders: Seq[EnvironmentAlertBuilder] = Seq(
        EnvironmentAlertBuilder("team-telemetry-both").inProduction(Set(Severity.Warning, Severity.Critical)),
        EnvironmentAlertBuilder("team-telemetry-warning-only").inProduction(Set(Severity.Warning)),
        EnvironmentAlertBuilder("team-telemetry-critical-only").inProduction(Set(Severity.Critical)),
        // This one should not appear in the output YAML
        EnvironmentAlertBuilder("team-telemetry-non-prod-one").disableProduction().inIntegration(Set(Severity.Warning, Severity.Critical))
      )
      IntegrationsYamlBuilder.generate(environmentAlertBuilders, currentEnvironment, testFile)

      val yamlFromDisk = Source.fromFile(testFile).getLines().mkString("\n")

      yamlFromDisk shouldBe
        """integrations:
          |  - name: team-telemetry-both
          |    severitiesEnabled:
          |      - warning
          |      - critical
          |  - name: team-telemetry-warning-only
          |    severitiesEnabled:
          |      - warning
          |  - name: team-telemetry-critical-only
          |    severitiesEnabled:
          |      - critical""".stripMargin

    }
  }

}
