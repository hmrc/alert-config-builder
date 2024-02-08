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
import uk.gov.hmrc.alertconfig.builder.yaml.{YamlAverageCPUThresholdAlert, YamlBuilder, YamlContainerKillThresholdAlert, YamlExceptionThresholdAlert, YamlHttp5xxPercentThresholdAlert}

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
    "containerKillThreshold should be set to defined threshold" in {
      val threshold = 56
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withContainerKillThreshold(threshold, AlertingPlatform.Grafana)

      val output = YamlBuilder.convertAlerts(config, Environment.Production)

      output.containerKillThreshold shouldBe Some(YamlContainerKillThresholdAlert(threshold))
    }

    "averageCPUThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))

      val output = YamlBuilder.convertAlerts(config, Environment.Integration)

      output.averageCPUThreshold shouldBe None
    }

    "averageCPUThreshold should be set to defined threshold" in {
      val threshold = 60
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withAverageCPUThreshold(threshold)

      val output = YamlBuilder.convertAlerts(config, Environment.Integration)

      output.averageCPUThreshold shouldBe Some(YamlAverageCPUThresholdAlert(threshold))
    }

    "ErrorsLoggedThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))

      val output = YamlBuilder.convertAlerts(config, Environment.Integration)

      output.errorsLoggedThreshold shouldBe None
    }

    "ExceptionThreshold should be 2 by default" in {
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))

      val output = YamlBuilder.convertAlerts(config, Environment.Integration)

      output.exceptionThreshold shouldBe Some(YamlExceptionThresholdAlert(2, "critical"))
    }

    "Http5xxPercentThreshold should be 100% by default" in {
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))

      val output = YamlBuilder.convertAlerts(config, Environment.Integration)

      output.http5xxPercentThreshold shouldBe Some(YamlHttp5xxPercentThresholdAlert(100.0, "critical"))
    }

    "Http5xxThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))

      val output = YamlBuilder.convertAlerts(config, Environment.Integration)

      output.http5xxThreshold shouldBe None
    }

    "HttpStatusPercentThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))

      val output = YamlBuilder.convertAlerts(config, Environment.Integration)

      output.httpStatusPercentThresholds shouldBe None
    }

    "HttpStatusThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))

      val output = YamlBuilder.convertAlerts(config, Environment.Integration)

      output.httpStatusThresholds shouldBe None
    }

    "HttpTrafficThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))

      val output = YamlBuilder.convertAlerts(config, Environment.Integration)

      output.httpTrafficThresholds shouldBe None
    }

    "LogMessageThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))

      val output = YamlBuilder.convertAlerts(config, Environment.Integration)

      output.logMessageThresholds shouldBe None
    }

    "MetricsThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))

      val output = YamlBuilder.convertAlerts(config, Environment.Integration)

      output.metricsThresholds shouldBe None
    }

    "TotalHttpRequestThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))

      val output = YamlBuilder.convertAlerts(config, Environment.Integration)

      output.totalHttpRequestThreshold shouldBe None
    }
  }
}
