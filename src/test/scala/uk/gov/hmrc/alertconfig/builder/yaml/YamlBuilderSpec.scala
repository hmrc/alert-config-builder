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

package uk.gov.hmrc.alertconfig.builder

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.alertconfig.builder.yaml.{YamlBuilder, YamlContainerKillThresholdAlert}

class YamlBuilderSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    System.setProperty("app-config-path", "src/test/resources/app-config")
    System.setProperty("zone-mapping-path", "src/test/resources/zone-to-service-domain-mapping.yml")
  }

  "convert(Seq[AlertConfig])" should {
    "when supplied an empty sequence it should return an empty list" in {
      YamlBuilder.convert(Seq(), Environment.Qa) shouldBe List()
    }
  }

  "convertAlerts(alertConfigBuilder)" should {
    "return Alerts" in {

      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withContainerKillThreshold(56, AlertingPlatform.Grafana)

      val output = YamlBuilder.convertAlerts(config, Environment.Production)

      output.containerKillThreshold shouldBe Some(YamlContainerKillThresholdAlert(56))
    }
  }
}
