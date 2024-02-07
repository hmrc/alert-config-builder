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
import uk.gov.hmrc.alertconfig.builder.{AlertConfigBuilder, AlertingPlatform, Environment}

class MigrationTests extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    System.setProperty("app-config-path", "src/test/resources/app-config")
    System.setProperty("zone-mapping-path", "src/test/resources/zone-to-service-domain-mapping.yml")
  }

  "convertAlerts(alertConfigBuilder)" should {
    "averageCPUThreshold should be enabled if alertingPlatform is Grafana" in {
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withAverageCPUThreshold(60, AlertingPlatform.Grafana)

      val output = YamlBuilder.convertAlerts(config, Environment.Production)

      output.averageCPUThreshold shouldBe Some(YamlAverageCPUThresholdAlert(60))
    }

    "averageCPUThreshold should be enabled by default in integration" in {
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withAverageCPUThreshold(60)

      val output = YamlBuilder.convertAlerts(config, Environment.Integration)

      output.averageCPUThreshold shouldBe Some(YamlAverageCPUThresholdAlert(60))
    }

    "averageCPUThreshold should be disabled by default in production" in {
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withAverageCPUThreshold(60)

      val output = YamlBuilder.convertAlerts(config, Environment.Production)

      output.averageCPUThreshold shouldBe None
    }

    "averageCPUThreshold should be disabled if alertingPlatform is Sensu" in {
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withAverageCPUThreshold(60, AlertingPlatform.Sensu)

      val output = YamlBuilder.convertAlerts(config, Environment.Integration)

      output.averageCPUThreshold shouldBe None
    }
  }
}
