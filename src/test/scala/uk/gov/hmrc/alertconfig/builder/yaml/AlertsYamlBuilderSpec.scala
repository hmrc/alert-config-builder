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
import uk.gov.hmrc.alertconfig.builder._

class AlertsYamlBuilderSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  "convert(Seq[AlertConfig])" should {
    "when supplied an empty sequence it should return an empty list" in {
      AlertsYamlBuilder.convert(Seq(), Environment.Qa) shouldBe List()
    }
  }

  "convert()" should {
    "filter out alerting configuration for services without an entry in app-config-env (meaning they are undeployed)" in {
      val config = AlertConfigBuilder("service-without-app-config-entry", integrations = Seq("h1", "h2"))
        .withContainerKillThreshold(10)

      val res = AlertsYamlBuilder.convert(
        alertConfigBuilder = config,
        environmentDefinedIntegrations = Set("h1"),
        integrationSeveritiesForEnv = Map("h1" -> Set(Severity.Warning, Severity.Critical))
      )

      res shouldBe None
    }
  }

  "If alert type is defined with a warning severity and integration foo, and environment config says foo can only receive critical alerts" should {
    "not create the alerts defined with a warning severity" in {

      val envConfig = Seq(
        EnvironmentAlertBuilder("integration-non-prod", enabledEnvironments = Map(Environment.Qa -> Set(Severity.Critical)))
      )

      val alertConfigBuilders = Seq(
        AlertConfigBuilder(
          serviceName = "service1",
          integrations = Seq("integration-non-prod")
        )
          .withErrorsLoggedThreshold(4)
          .withExceptionThreshold(1, AlertSeverity.Warning)
          .withHttp5xxThreshold(1, AlertSeverity.Warning)
          .withHttp5xxPercentThreshold(1, severity = AlertSeverity.Warning)
          .withHttp90PercentileResponseTimeThreshold(Http90PercentileResponseTimeThreshold(warning = Some(1), critical = None))
          .withHttpAbsolutePercentSplitThreshold(HttpAbsolutePercentSplitThreshold(severity = AlertSeverity.Warning))
          .withHttpAbsolutePercentSplitDownstreamServiceThreshold(HttpAbsolutePercentSplitDownstreamServiceThreshold(severity =
            AlertSeverity.Warning))
          .withHttpAbsolutePercentSplitDownstreamHodThreshold(HttpAbsolutePercentSplitDownstreamHodThreshold(severity = AlertSeverity.Warning))
          .withContainerKillThreshold(2)
          .withHttpTrafficThreshold(HttpTrafficThreshold(warning = Some(1), critical = Some(2)))
          .withHttpStatusThreshold(HttpStatusThreshold(httpStatus = HttpStatus.HTTP_STATUS_500, severity = AlertSeverity.Warning))
          .withHttpStatusThreshold(HttpStatusThreshold(httpStatus = HttpStatus.HTTP_STATUS_501, severity = AlertSeverity.Critical))
          .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_500, severity = AlertSeverity.Warning))
          .withLogMessageThreshold("HelloWorld", 1, lessThanMode = false, AlertSeverity.Warning)
          .withTotalHttpRequestThreshold(2)
          .withAverageCPUThreshold(1)
      )

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder]            = alertConfigBuilders
        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val res = AlertsYamlBuilder.convert(fakeConfig, Environment.Qa)
      res shouldBe Seq(
        ServiceConfig(
          service = "service1",
          alerts = Alerts(
            averageCPUThreshold = Some(YamlAverageCPUThresholdAlert(1)),
            containerKillThreshold = Some(YamlContainerKillThresholdAlert(2)),
            errorsLoggedThreshold = Some(YamlErrorsLoggedThresholdAlert(4)),
            exceptionThreshold = None,
            logMessageThresholds = None,
            http5xxThreshold = None,
            http5xxPercentThreshold = None,
            httpStatusPercentThresholds = None,
            httpStatusThresholds = Some(Seq(YamlHttpStatusThresholdAlert(1, "ALL_METHODS", 501, "critical"))),
            httpTrafficThresholds = Some(Seq(YamlHttpTrafficThresholdAlert(2, 5, "critical"))),
            totalHttpRequestThreshold = Some(YamlTotalHttpRequestThresholdAlert(2)),
            http90PercentileResponseTimeThreshold = None
          ),
          pagerduty = Seq(
            PagerDuty(integrationKeyName = "integration-non-prod")
          )
        )
      )

    }
  }

  "If integration 'foo' is enabled in an env for severities warning AND critical, and integration 'bar' is enabled in the same env for only critical alerts" should {
    "still create alerts defined with a warning severity" in {

      val envConfig = Seq(
        EnvironmentAlertBuilder("foo", enabledEnvironments = Map(Environment.Qa -> Set(Severity.Critical, Severity.Warning))),
        EnvironmentAlertBuilder("bar", enabledEnvironments = Map(Environment.Qa -> Set(Severity.Critical)))
      )

      val alertConfigBuilders = Seq(
        AlertConfigBuilder(
          serviceName = "service1",
          integrations = Seq("foo", "bar")
        )
          .withExceptionThreshold(9, AlertSeverity.Warning)
          .disableContainerKillThreshold()
          .disableHttp5xxPercentThreshold()
      )

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder]            = alertConfigBuilders
        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val res = AlertsYamlBuilder.convert(fakeConfig, Environment.Qa)
      res shouldBe Seq(
        ServiceConfig(
          service = "service1",
          alerts = Alerts(
            averageCPUThreshold = None,
            containerKillThreshold = None,
            errorsLoggedThreshold = None,
            exceptionThreshold = Some(YamlExceptionThresholdAlert(9, "warning")),
            logMessageThresholds = None,
            http5xxThreshold = None,
            http5xxPercentThreshold = None,
            httpStatusPercentThresholds = None,
            httpStatusThresholds = None,
            httpTrafficThresholds = None,
            totalHttpRequestThreshold = None,
            http90PercentileResponseTimeThreshold = None
          ),
          pagerduty = Seq(
            PagerDuty(integrationKeyName = "foo"),
            PagerDuty(integrationKeyName = "bar")
          )
        )
      )

    }
  }

  "If integration 'foo' is enabled in an env for severities warning AND critical, and integration 'bar' is enabled in the same env for only warning alerts" should {
    "still create alerts defined with a critical severity" in {

      val envConfig = Seq(
        EnvironmentAlertBuilder("foo", enabledEnvironments = Map(Environment.Qa -> Set(Severity.Critical, Severity.Warning))),
        EnvironmentAlertBuilder("bar", enabledEnvironments = Map(Environment.Qa -> Set(Severity.Warning)))
      )

      val alertConfigBuilders = Seq(
        AlertConfigBuilder(
          serviceName = "service1",
          integrations = Seq("foo", "bar")
        )
          .withExceptionThreshold(9, AlertSeverity.Critical)
          .disableContainerKillThreshold()
          .disableHttp5xxPercentThreshold()
      )

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder]            = alertConfigBuilders
        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val res = AlertsYamlBuilder.convert(fakeConfig, Environment.Qa)
      res shouldBe Seq(
        ServiceConfig(
          service = "service1",
          alerts = Alerts(
            averageCPUThreshold = None,
            containerKillThreshold = None,
            errorsLoggedThreshold = None,
            exceptionThreshold = Some(YamlExceptionThresholdAlert(9, "critical")),
            logMessageThresholds = None,
            http5xxThreshold = None,
            http5xxPercentThreshold = None,
            httpStatusPercentThresholds = None,
            httpStatusThresholds = None,
            httpTrafficThresholds = None,
            totalHttpRequestThreshold = None,
            http90PercentileResponseTimeThreshold = None
          ),
          pagerduty = Seq(
            PagerDuty(integrationKeyName = "foo"),
            PagerDuty(integrationKeyName = "bar")
          )
        )
      )

    }
  }

  "If alert type is defined with a critical severity and integration foo, and environment config says foo can only receive warning alerts" should {
    "not create the alerts defined with a critical severity" in {

      val envConfig = Seq(
        EnvironmentAlertBuilder("integration-non-prod", enabledEnvironments = Map(Environment.Qa -> Set(Severity.Warning)))
      )

      val alertConfigBuilders = Seq(
        AlertConfigBuilder(
          serviceName = "service1",
          integrations = Seq("integration-non-prod")
        )
          .withErrorsLoggedThreshold(4)
          .withExceptionThreshold(1, AlertSeverity.Critical)
          .withHttp5xxThreshold(1, AlertSeverity.Critical)
          .withHttp5xxPercentThreshold(1, severity = AlertSeverity.Critical)
          .withHttp90PercentileResponseTimeThreshold(Http90PercentileResponseTimeThreshold(warning = Some(2), critical = Some(1)))
          .withHttpAbsolutePercentSplitThreshold(HttpAbsolutePercentSplitThreshold(severity = AlertSeverity.Critical))
          .withHttpAbsolutePercentSplitDownstreamServiceThreshold(HttpAbsolutePercentSplitDownstreamServiceThreshold(severity =
            AlertSeverity.Critical))
          .withHttpAbsolutePercentSplitDownstreamHodThreshold(HttpAbsolutePercentSplitDownstreamHodThreshold(severity = AlertSeverity.Critical))
          .withContainerKillThreshold(2)
          .withHttpTrafficThreshold(HttpTrafficThreshold(warning = Some(1), critical = Some(2)))
          .withHttpStatusThreshold(HttpStatusThreshold(httpStatus = HttpStatus.HTTP_STATUS_500, severity = AlertSeverity.Warning))
          .withHttpStatusThreshold(HttpStatusThreshold(httpStatus = HttpStatus.HTTP_STATUS_501, severity = AlertSeverity.Critical))
          .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_500, severity = AlertSeverity.Critical))
          .withLogMessageThreshold("HelloWorld", 1, lessThanMode = false, AlertSeverity.Critical)
          .withTotalHttpRequestThreshold(2)
          .withAverageCPUThreshold(1)
      )

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder]            = alertConfigBuilders
        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val res = AlertsYamlBuilder.convert(fakeConfig, Environment.Qa)
      res shouldBe Seq(
        ServiceConfig(
          service = "service1",
          alerts = Alerts(
            averageCPUThreshold = Some(YamlAverageCPUThresholdAlert(1)),
            containerKillThreshold = Some(YamlContainerKillThresholdAlert(2)),
            errorsLoggedThreshold = Some(YamlErrorsLoggedThresholdAlert(4)),
            exceptionThreshold = None,
            logMessageThresholds = None,
            http5xxThreshold = None,
            http5xxPercentThreshold = None,
            httpStatusPercentThresholds = None,
            httpStatusThresholds = Some(Seq(YamlHttpStatusThresholdAlert(1, "ALL_METHODS", 500, "warning"))),
            httpTrafficThresholds = Some(Seq(YamlHttpTrafficThresholdAlert(1, 5, "warning"))),
            totalHttpRequestThreshold = Some(YamlTotalHttpRequestThresholdAlert(2)),
            http90PercentileResponseTimeThreshold = Some(Seq(YamlHttp90PercentileResponseTimeThresholdAlert(15, 2, "warning")))
          ),
          pagerduty = Seq(
            PagerDuty(integrationKeyName = "integration-non-prod")
          )
        )
      )

    }
  }

  "convertAlerts(alertConfigBuilder)" should {
    "containerKillThreshold should be set to defined threshold" in {
      val threshold = 56
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withContainerKillThreshold(threshold)

      val output = AlertsYamlBuilder.convertAlerts(config)

      output.containerKillThreshold shouldBe Some(YamlContainerKillThresholdAlert(threshold))
    }

    "averageCPUThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))

      val output = AlertsYamlBuilder.convertAlerts(config)

      output.averageCPUThreshold shouldBe None
    }

    "averageCPUThreshold should be set to defined threshold" in {
      val threshold = 60
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withAverageCPUThreshold(threshold)

      val output = AlertsYamlBuilder.convertAlerts(config)

      output.averageCPUThreshold shouldBe Some(YamlAverageCPUThresholdAlert(threshold))
    }

    "ErrorsLoggedThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))

      val output = AlertsYamlBuilder.convertAlerts(config)

      output.errorsLoggedThreshold shouldBe None
    }

    "ExceptionThreshold should be 2 by default" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))

      val output = AlertsYamlBuilder.convertAlerts(config)

      output.exceptionThreshold shouldBe Some(YamlExceptionThresholdAlert(2, "critical"))
    }

    "Http5xxPercentThreshold should be 100% by default" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))

      val output = AlertsYamlBuilder.convertAlerts(config)

      output.http5xxPercentThreshold shouldBe Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical"))
    }

    "Http5xxThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))

      val output = AlertsYamlBuilder.convertAlerts(config)

      output.http5xxThreshold shouldBe None
    }

    "httpAbsolutePercentSplitThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))

      val output = AlertsYamlBuilder.convertAlerts(config)

      output.httpAbsolutePercentSplitThreshold shouldBe None
    }

    "HttpStatusPercentThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))

      val output = AlertsYamlBuilder.convertAlerts(config)

      output.httpStatusPercentThresholds shouldBe None
    }

    "HttpStatusThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))

      val output = AlertsYamlBuilder.convertAlerts(config)

      output.httpStatusThresholds shouldBe None
    }

    "HttpTrafficThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))

      val output = AlertsYamlBuilder.convertAlerts(config)

      output.httpTrafficThresholds shouldBe None
    }

    "LogMessageThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))

      val output = AlertsYamlBuilder.convertAlerts(config)

      output.logMessageThresholds shouldBe None
    }

    "TotalHttpRequestThreshold should be disabled by default" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))

      val output = AlertsYamlBuilder.convertAlerts(config)

      output.totalHttpRequestThreshold shouldBe None
    }
  }

}
