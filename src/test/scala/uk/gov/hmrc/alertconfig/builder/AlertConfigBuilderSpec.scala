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
import uk.gov.hmrc.alertconfig.builder.yaml.{Alerts, AlertsYamlBuilder, PagerDuty, YamlAverageCPUThresholdAlert, YamlContainerKillThresholdAlert, YamlErrorsLoggedThresholdAlert, YamlExceptionThresholdAlert, YamlHttp5xxPercentThresholdAlert, YamlHttp5xxThresholdAlert, YamlHttpAbsolutePercentSplitDownstreamHodThresholdAlert, YamlHttpAbsolutePercentSplitDownstreamServiceThresholdAlert, YamlHttpAbsolutePercentSplitThresholdAlert, YamlHttpStatusPercentThresholdAlert, YamlHttpStatusThresholdAlert, YamlHttpTrafficThresholdAlert, YamlTotalHttpRequestThresholdAlert}

class AlertConfigBuilderSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    System.setProperty("app-config-path", "src/test/resources/app-config")
    System.setProperty("zone-mapping-path", "src/test/resources/zone-to-service-domain-mapping.yml")
  }

  private val integration = "integration"

  private val envConfig = Seq(
    EnvironmentAlertBuilder(integration).inProduction()
  )

  "AlertConfigBuilder" should {
    "build correct config" in {
      val config = AlertConfigBuilder("service1", integrations = Seq("h1", "h2"))
        .withContainerKillThreshold(56)

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(config)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = Seq(
          EnvironmentAlertBuilder("h1").inProduction(),
          EnvironmentAlertBuilder("h2").inProduction()
        )
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(56)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        averageCPUThreshold = None,
        errorsLoggedThreshold = None,
        logMessageThresholds = None,
        http5xxThreshold = None,
        httpAbsolutePercentSplitThreshold = None,
        httpAbsolutePercentSplitDownstreamHodThreshold = None,
        httpAbsolutePercentSplitDownstreamServiceThreshold = None,
        httpStatusPercentThresholds = None,
        httpStatusThresholds = None,
        httpTrafficThresholds = None,
        totalHttpRequestThreshold = None,
        metricsThresholds = None,
        http90PercentileResponseTimeThreshold = None
      )

      service1Config.service shouldBe "service1"
      service1Config.alerts shouldBe expected
      service1Config.pagerduty shouldBe Seq(PagerDuty("h1"), PagerDuty("h2"))
    }

    "build correct config for platform service" in {
      val platformServiceConfig = AlertConfigBuilder("ingress-gateway", integrations = Seq("h1", "h2"))
        .isPlatformService(true)
        .withContainerKillThreshold(1)
        .withIntegrations(integration)

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(platformServiceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical"))
      )

      service1Config.service shouldBe "ingress-gateway"
      service1Config.alerts shouldBe expected
    }

    "Returns None when app config file not found" in {
      val serviceConfig = AlertConfigBuilder("absent-service", integrations = Seq(integration))

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 0
    }

    "configure http status threshold with given thresholds and severities" in {
      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withHttpStatusThreshold(HttpStatusThreshold(HttpStatus.HTTP_STATUS_502, 2, AlertSeverity.Warning, HttpMethod.Post))
        .withHttpStatusThreshold(HttpStatusThreshold(HttpStatus.HTTP_STATUS_504, 4))

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        httpStatusThresholds = Some(
          Seq(
            YamlHttpStatusThresholdAlert(2, "POST", 502, "warning"),
            YamlHttpStatusThresholdAlert(4, "ALL_METHODS", 504, "critical")
          ))
      )

      service1Config.alerts shouldBe expected
    }

    "configure http status threshold with given status code using default threshold" in {
      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withHttpStatusThreshold(HttpStatusThreshold(HttpStatus.HTTP_STATUS(404)))

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        httpStatusThresholds = Some(
          Seq(
            YamlHttpStatusThresholdAlert(1, "ALL_METHODS", 404, "critical")
          ))
      )

      service1Config.alerts shouldBe expected
    }

    "configure http status percent threshold with given values" in {
      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_502, 2.2, AlertSeverity.Warning, HttpMethod.Post))
        .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_504, 4.4))

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        httpStatusPercentThresholds = Some(
          Seq(
            YamlHttpStatusPercentThresholdAlert(2.2, "POST", 502, "warning"),
            YamlHttpStatusPercentThresholdAlert(4.4, "ALL_METHODS", 504, "critical")
          ))
      )

      service1Config.alerts shouldBe expected
    }

    "configure http 5xx threshold severity with given thresholds and unspecified severity" in {
      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withHttp5xxThreshold(2)

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        http5xxThreshold = Some(YamlHttp5xxThresholdAlert(2, "critical"))
      )

      service1Config.alerts shouldBe expected
    }

    "configure httpTrafficThreshold with given thresholds" in {
      val threshold = HttpTrafficThreshold(Some(10), Some(5), 35)
      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withHttpTrafficThreshold(threshold)

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        httpTrafficThresholds = Some(
          Seq(
            YamlHttpTrafficThresholdAlert(10, 35, "warning"),
            YamlHttpTrafficThresholdAlert(5, 35, "critical")
          ))
      )

      service1Config.alerts shouldBe expected
    }

    "throw exception if httpTrafficThreshold is defined multiple times" in {
      an[Exception] should be thrownBy AlertConfigBuilder("service1")
        .withHttpTrafficThreshold(HttpTrafficThreshold(Some(10), Some(5), 35))
        .withHttpTrafficThreshold(HttpTrafficThreshold(Some(10), Some(5), 35))
    }

    "configure HttpAbsolutePercentSplitThreshold with given thresholds" in {
      val percent = 10.2
      val crossOver = 20
      val absolute = 30
      val hysteresis = 1.2
      val excludeSpikes = 2
      val filter = "Some error"
      val severity = AlertSeverity.Warning

      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withHttpAbsolutePercentSplitThreshold(
          HttpAbsolutePercentSplitThreshold(percent, crossOver, absolute, hysteresis, excludeSpikes, filter, severity))

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        httpAbsolutePercentSplitThreshold = Some(
          Seq(
            YamlHttpAbsolutePercentSplitThresholdAlert(percent, crossOver, absolute, hysteresis, excludeSpikes, filter, "warning")
          ))
      )

      service1Config.alerts shouldBe expected
    }

    "configure HttpAbsolutePercentSplitDownstreamServiceThreshold with given thresholds" in {
      val percent = 10.2
      val crossOver = 20
      val absolute = 30
      val hysteresis = 1.2
      val excludeSpikes = 2
      val filter = "Some error"
      val target = "service.invalid"
      val severity = AlertSeverity.Warning

      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withHttpAbsolutePercentSplitDownstreamServiceThreshold(
          HttpAbsolutePercentSplitDownstreamServiceThreshold(percent, crossOver, absolute, hysteresis, excludeSpikes, filter, target, severity))

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        httpAbsolutePercentSplitDownstreamServiceThreshold = Some(
          Seq(
            YamlHttpAbsolutePercentSplitDownstreamServiceThresholdAlert(percent, crossOver, absolute, hysteresis, excludeSpikes, filter, target, "warning")
          ))
      )

      service1Config.alerts shouldBe expected
    }

    "configure HttpAbsolutePercentSplitDownstreamHodThreshold with given thresholds" in {
      val percent = 10.2
      val crossOver = 20
      val absolute = 30
      val hysteresis = 1.2
      val excludeSpikes = 2
      val filter = "Some error"
      val target = "hod-endpoint"
      val severity = AlertSeverity.Warning

      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withHttpAbsolutePercentSplitDownstreamHodThreshold(
          HttpAbsolutePercentSplitDownstreamHodThreshold(percent, crossOver, absolute, hysteresis, excludeSpikes, filter, target, severity))

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        httpAbsolutePercentSplitDownstreamHodThreshold = Some(
          Seq(
            YamlHttpAbsolutePercentSplitDownstreamHodThresholdAlert(percent, crossOver, absolute, hysteresis, excludeSpikes, filter, target, "warning")
          ))
      )

      service1Config.alerts shouldBe expected
    }

    "configure ExceptionThreshold with given values" in {
      val threshold = 12
      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withExceptionThreshold(threshold)

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(12, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical"))
      )

      service1Config.alerts shouldBe expected
    }

    "configure ExceptionThreshold with optional parameter severity" in {
      val threshold = 12
      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withExceptionThreshold(threshold, AlertSeverity.Warning)

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(12, "warning")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical"))
      )

      service1Config.alerts shouldBe expected
    }

    "configure ErrorsLoggedThreshold with given values" in {
      val threshold = 12
      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withErrorsLoggedThreshold(threshold)

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        errorsLoggedThreshold = Some(YamlErrorsLoggedThresholdAlert(12))
      )

      service1Config.alerts shouldBe expected
    }

    "configure http5xxPercentThreshold with given values" in {
      val threshold = 13.3
      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withHttp5xxPercentThreshold(threshold)

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(threshold, 0, "critical"))
      )

      service1Config.alerts shouldBe expected
    }

    "configure http5xxPercentThreshold with count and percentage thresholds" in {
      val threshold = 13.3
      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withHttp5xxPercentThreshold(threshold, 10)

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(threshold, 10, "critical"))
      )

      service1Config.alerts shouldBe expected
    }

    "configure averageCPUThreshold with given thresholds" in {
      val threshold = 15
      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withAverageCPUThreshold(threshold)

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        averageCPUThreshold = Some(YamlAverageCPUThresholdAlert(threshold))
      )

      service1Config.alerts shouldBe expected
    }

    "use the configured value for containerKillThreshold" in {
      val threshold = 3
      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withContainerKillThreshold(threshold)

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(threshold)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical"))
      )

      service1Config.alerts shouldBe expected
    }

    "configure http request count threshold with given threshold" in {
      val serviceConfig = AlertConfigBuilder("service1", integrations = Seq(integration))
        .withTotalHttpRequestThreshold(500)

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = Seq(serviceConfig)

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 1
      val service1Config = output(0)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        totalHttpRequestThreshold = Some(YamlTotalHttpRequestThresholdAlert(500))
      )

      service1Config.alerts shouldBe expected
    }
  }
}
