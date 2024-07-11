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
import uk.gov.hmrc.alertconfig.builder.HttpStatus.HTTP_STATUS
import uk.gov.hmrc.alertconfig.builder.{
  AlertConfigBuilder,
  AlertSeverity,
  AlertingPlatform,
  Environment,
  HttpAbsolutePercentSplitThreshold,
  HttpStatusPercentThreshold,
  HttpStatusThreshold,
  HttpTrafficThreshold,
  MetricsThreshold
}

class MigrationTests extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    System.setProperty("app-config-path", "src/test/resources/app-config")
    System.setProperty("zone-mapping-path", "src/test/resources/zone-to-service-domain-mapping.yml")
  }

  "convertAlerts(alertConfigBuilder)" should {
    "averageCPUThreshold should be enabled if alertingPlatform is Grafana" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withAverageCPUThreshold(60, AlertingPlatform.Grafana)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.averageCPUThreshold shouldBe Some(YamlAverageCPUThresholdAlert(60))
    }

    "averageCPUThreshold should be enabled by default in integration" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withAverageCPUThreshold(60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.averageCPUThreshold shouldBe Some(YamlAverageCPUThresholdAlert(60))
    }

    "averageCPUThreshold should be enabled by default in production" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withAverageCPUThreshold(60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.averageCPUThreshold shouldBe Some(YamlAverageCPUThresholdAlert(60))
    }

    "averageCPUThreshold should be disabled if alertingPlatform is Sensu" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withAverageCPUThreshold(60, AlertingPlatform.Sensu)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.averageCPUThreshold shouldBe None
    }

    "containerKillThreshold should be enabled if alertingPlatform is Grafana" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withContainerKillThreshold(60, AlertingPlatform.Grafana)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.containerKillThreshold shouldBe Some(YamlContainerKillThresholdAlert(60))
    }

    "containerKillThreshold should be disabled if alertingPlatform is Sensu" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withContainerKillThreshold(60, AlertingPlatform.Sensu)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.containerKillThreshold shouldBe None
    }

    "containerKillThreshold should be enabled by default in integration" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withContainerKillThreshold(60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.containerKillThreshold shouldBe Some(YamlContainerKillThresholdAlert(60))
    }

    "containerKillThreshold should be enabled by default in production" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withContainerKillThreshold(60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.containerKillThreshold shouldBe Some(YamlContainerKillThresholdAlert(60))
    }

    "ErrorsLoggedThreshold should be enabled if alertingPlatform is Grafana" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withErrorsLoggedThreshold(60, AlertingPlatform.Grafana)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.errorsLoggedThreshold shouldBe Some(YamlErrorsLoggedThresholdAlert(60))
    }

    "ErrorsLoggedThreshold should be disabled if alertingPlatform is Sensu" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withErrorsLoggedThreshold(60, AlertingPlatform.Sensu)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.errorsLoggedThreshold shouldBe None
    }

    "ErrorsLoggedThreshold should be enabled by default in integration" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withErrorsLoggedThreshold(60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.errorsLoggedThreshold shouldBe Some(YamlErrorsLoggedThresholdAlert(60))
    }

    "ErrorsLoggedThreshold should be enabled by default in production" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withErrorsLoggedThreshold(60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.errorsLoggedThreshold shouldBe Some(YamlErrorsLoggedThresholdAlert(60))
    }

    "ExceptionThreshold should be enabled if alertingPlatform is Grafana" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withExceptionThreshold(60, alertingPlatform = AlertingPlatform.Grafana)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.exceptionThreshold shouldBe Some(YamlExceptionThresholdAlert(60, "critical"))
    }

    "ExceptionThreshold should be disabled if alertingPlatform is Sensu" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withExceptionThreshold(60, alertingPlatform = AlertingPlatform.Sensu)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.exceptionThreshold shouldBe None
    }

    "ExceptionThreshold should be enabled by default in integration" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withExceptionThreshold(60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.exceptionThreshold shouldBe Some(YamlExceptionThresholdAlert(60, "critical"))
    }

    "ExceptionThreshold should be disabled by default in production" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withExceptionThreshold(60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.exceptionThreshold shouldBe Some(YamlExceptionThresholdAlert(60, "critical"))
    }

    "Http5xxPercentThreshold should be enabled if alertingPlatform is Grafana" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttp5xxPercentThreshold(60, alertingPlatform = AlertingPlatform.Grafana)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.http5xxPercentThreshold shouldBe Some(YamlHttp5xxPercentThresholdAlert(60, 0, "critical"))
    }

    "Http5xxPercentThreshold should be disabled if alertingPlatform is Sensu" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttp5xxPercentThreshold(60, alertingPlatform = AlertingPlatform.Sensu)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.http5xxPercentThreshold shouldBe None
    }

    "Http5xxPercentThreshold should be enabled by default in integration" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttp5xxPercentThreshold(60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.http5xxPercentThreshold shouldBe Some(YamlHttp5xxPercentThresholdAlert(60, 0, "critical"))
    }

    "Http5xxPercentThreshold should be enabled by default in integration, supporting minimumHttp5xxCountThreshold" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttp5xxPercentThreshold(60, 5)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.http5xxPercentThreshold shouldBe Some(YamlHttp5xxPercentThresholdAlert(60, 5, "critical"))
    }

    "Http5xxPercentThreshold should be disabled by default in production" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttp5xxPercentThreshold(60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.http5xxPercentThreshold shouldBe None
    }

    "Http5xxThreshold should be enabled if alertingPlatform is Grafana" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttp5xxThreshold(60, alertingPlatform = AlertingPlatform.Grafana)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.http5xxThreshold shouldBe Some(YamlHttp5xxThresholdAlert(60, "critical"))
    }

    "Http5xxThreshold should be disabled if alertingPlatform is Sensu" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttp5xxThreshold(60, alertingPlatform = AlertingPlatform.Sensu)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.http5xxThreshold shouldBe None
    }

    "Http5xxThreshold should be enabled by default in integration" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttp5xxThreshold(60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.http5xxThreshold shouldBe Some(YamlHttp5xxThresholdAlert(60, "critical"))
    }

    "Http5xxThreshold should be disabled by default in production" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttp5xxPercentThreshold(60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.http5xxThreshold shouldBe None
    }

    "httpAbsolutePercentSplitThreshold should be enabled if alertPlatform is Grafana" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttpAbsolutePercentSplitThreshold(HttpAbsolutePercentSplitThreshold(
          percentThreshold = 50,
          crossOver = 1,
          absoluteThreshold = 2,
          hysteresis = 1,
          excludeSpikes = 2,
          errorFilter = "status:>501",
          severity = AlertSeverity.Critical,
          alertingPlatform = AlertingPlatform.Grafana
        ))

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.httpAbsolutePercentSplitThreshold shouldBe Some(
        List(YamlHttpAbsolutePercentSplitThresholdAlert(50, 1, 2, 1, 2, "status:>501", "critical")))
    }

    "httpAbsolutePercentSplitThreshold should be disabled if alertPlatform is Sensu" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttpAbsolutePercentSplitThreshold(HttpAbsolutePercentSplitThreshold(
          percentThreshold = 50,
          crossOver = 1,
          absoluteThreshold = 2,
          hysteresis = 1,
          excludeSpikes = 2,
          errorFilter = "status:>501",
          severity = AlertSeverity.Critical,
          alertingPlatform = AlertingPlatform.Sensu
        ))

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)
      output.httpAbsolutePercentSplitThreshold shouldBe None
    }

    "HttpStatusPercentThreshold should be enabled if alertingPlatform is Grafana" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS(500), 60, alertingPlatform = AlertingPlatform.Grafana))

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.httpStatusPercentThresholds shouldBe Some(List(YamlHttpStatusPercentThresholdAlert(60, "ALL_METHODS", 500, "critical")))
    }

    "HttpStatusPercentThreshold should be disabled if alertingPlatform is Sensu" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS(500), 60, alertingPlatform = AlertingPlatform.Sensu))

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.httpStatusPercentThresholds shouldBe None
    }

    "HttpStatusPercentThreshold should be enabled by default in integration" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS(500), 60))

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.httpStatusPercentThresholds shouldBe Some(List(YamlHttpStatusPercentThresholdAlert(60, "ALL_METHODS", 500, "critical")))
    }

    "HttpStatusPercentThreshold should be enabled by default in production" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS(500), 60))

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.httpStatusPercentThresholds shouldBe Some(List(YamlHttpStatusPercentThresholdAlert(60.0, "ALL_METHODS", 500, "critical")))
    }

    "HttpStatusThreshold should be enabled if alertingPlatform is Grafana" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS(500), 60, alertingPlatform = AlertingPlatform.Grafana))

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.httpStatusThresholds shouldBe Some(List(YamlHttpStatusThresholdAlert(60, "ALL_METHODS", 500, "critical")))
    }

    "HttpStatusThreshold should be disabled if alertingPlatform is Sensu" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS(500), 60, alertingPlatform = AlertingPlatform.Sensu))

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.httpStatusThresholds shouldBe None
    }

    "HttpStatusThreshold should be enabled by default in integration" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS(500), 60))

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.httpStatusThresholds shouldBe Some(List(YamlHttpStatusThresholdAlert(60, "ALL_METHODS", 500, "critical")))
    }

    "HttpStatusThreshold should be enabled by default in production" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS(500), 60))

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.httpStatusThresholds shouldBe Some(List(YamlHttpStatusThresholdAlert(60, "ALL_METHODS", 500, "critical")))
    }

    "HttpTrafficThreshold should be enabled if alertingPlatform is Grafana" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttpTrafficThreshold(HttpTrafficThreshold(None, Some(60), alertingPlatform = AlertingPlatform.Grafana))

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.httpTrafficThresholds shouldBe Some(List(YamlHttpTrafficThresholdAlert(60, 5, "critical")))
    }

    "HttpTrafficThreshold should be disabled if alertingPlatform is Sensu" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttpTrafficThreshold(HttpTrafficThreshold(None, Some(60), alertingPlatform = AlertingPlatform.Sensu))

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.httpTrafficThresholds shouldBe None
    }

    "HttpTrafficThreshold should be enabled by default in integration" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttpTrafficThreshold(HttpTrafficThreshold(None, Some(60)))

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.httpTrafficThresholds shouldBe Some(List(YamlHttpTrafficThresholdAlert(60, 5, "critical")))
    }

    "HttpTrafficThreshold should be enabled by default in production" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withHttpTrafficThreshold(HttpTrafficThreshold(None, Some(60)))

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.httpTrafficThresholds shouldBe Some(List(YamlHttpTrafficThresholdAlert(60, 5, "critical")))
    }

    "LogMessageThreshold should be enabled if alertingPlatform is Grafana" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withLogMessageThreshold("LOG_MESSAGE", 60, alertingPlatform = AlertingPlatform.Grafana)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.logMessageThresholds shouldBe Some(
        List(YamlLogMessageThresholdAlert(count = 60, lessThanMode = false, message = "LOG_MESSAGE", severity = "critical")))
    }

    "LogMessageThreshold should be disabled if alertingPlatform is Sensu" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withLogMessageThreshold("LOG_MESSAGE", 60, alertingPlatform = AlertingPlatform.Sensu)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.logMessageThresholds shouldBe None
    }

    "LogMessageThreshold should be enabled by default in integration" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withLogMessageThreshold("LOG_MESSAGE", 60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.logMessageThresholds shouldBe Some(
        List(YamlLogMessageThresholdAlert(count = 60, lessThanMode = false, message = "LOG_MESSAGE", severity = "critical")))
    }

    "LogMessageThreshold should be enabled by default in production" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withLogMessageThreshold("LOG_MESSAGE", 60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.logMessageThresholds shouldBe Some(
        List(YamlLogMessageThresholdAlert(60, lessThanMode = false, "LOG_MESSAGE", "critical"))
      )
    }

    "TotalHttpRequestThreshold should be enabled if alertingPlatform is Grafana" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withTotalHttpRequestThreshold(60, AlertingPlatform.Grafana)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.totalHttpRequestThreshold shouldBe Some(YamlTotalHttpRequestThresholdAlert(60))
    }

    "TotalHttpRequestThreshold should be disabled if alertingPlatform is Sensu" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withTotalHttpRequestThreshold(60, AlertingPlatform.Sensu)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.totalHttpRequestThreshold shouldBe None
    }

    "TotalHttpRequestThreshold should be enabled by default in integration" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withTotalHttpRequestThreshold(60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Integration)

      output.totalHttpRequestThreshold shouldBe Some(YamlTotalHttpRequestThresholdAlert(60))
    }

    "TotalHttpRequestThreshold should be enabled by default in production" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withTotalHttpRequestThreshold(60)

      val output = AlertsYamlBuilder.convertAlerts(config, Environment.Production)

      output.totalHttpRequestThreshold shouldBe Some(YamlTotalHttpRequestThresholdAlert(60))
    }
  }

}
