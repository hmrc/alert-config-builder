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
import uk.gov.hmrc.alertconfig.builder.yaml.{
  Alerts,
  AlertsYamlBuilder,
  YamlAverageCPUThresholdAlert,
  YamlContainerKillThresholdAlert,
  YamlErrorsLoggedThresholdAlert,
  YamlExceptionThresholdAlert,
  YamlHttp5xxPercentThresholdAlert,
  YamlHttp5xxThresholdAlert,
  YamlHttp90PercentileResponseTimeThresholdAlert,
  YamlHttpAbsolutePercentSplitDownstreamHodThresholdAlert,
  YamlHttpAbsolutePercentSplitDownstreamServiceThresholdAlert,
  YamlHttpAbsolutePercentSplitThresholdAlert,
  YamlHttpStatusPercentThresholdAlert,
  YamlHttpStatusThresholdAlert,
  YamlHttpTrafficThresholdAlert,
  YamlLogMessageThresholdAlert,
  YamlTotalHttpRequestThresholdAlert
}

class TeamAlertConfigBuilderSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    System.setProperty("app-config-path", "src/test/resources/app-config")
    System.setProperty("zone-mapping-path", "src/test/resources/zone-to-service-domain-mapping.yml")
  }

  private val integration = "integration"

  private val envConfig = Seq(
    EnvironmentAlertBuilder(integration).inProduction()
  )

  "teamAlerts" should {
    "return TeamAlertConfigBuilder with correct default values" in {
      val alertConfigBuilder: TeamAlertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      alertConfigBuilder.integrations shouldBe List()
      alertConfigBuilder.averageCPUThreshold shouldBe None
      alertConfigBuilder.containerKillThreshold shouldBe Some(ContainerKillThreshold(1))
      alertConfigBuilder.exceptionThreshold shouldBe Some(ExceptionThreshold(2, AlertSeverity.Critical))
      alertConfigBuilder.http5xxPercentThreshold shouldBe Some(Http5xxPercentThreshold(100, severity = AlertSeverity.Critical))
      alertConfigBuilder.http5xxThreshold shouldBe None
      alertConfigBuilder.http90PercentileResponseTimeThresholds shouldBe List()
      alertConfigBuilder.httpAbsolutePercentSplitThresholds shouldBe List()
      alertConfigBuilder.httpStatusThresholds shouldBe List()
      alertConfigBuilder.httpTrafficThresholds shouldBe List()
      alertConfigBuilder.logMessageThresholds shouldBe List()
      alertConfigBuilder.totalHttpRequestThreshold shouldBe None
    }

    "result in identical defaults to AlertConfigBuilder" in {
      val teamAlertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1"))
      val alertConfigBuilder     = AlertConfigBuilder("service1")

      val teamConfigs = teamAlertConfigBuilder.build()

      teamConfigs shouldBe Seq(alertConfigBuilder)
    }

    "result in identical config to AlertConfigBuilder" in {
      val teamAlertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1"))
        .withAverageCPUThreshold(50)
        .withContainerKillThreshold(2)
        .withErrorsLoggedThreshold(5)
        .withExceptionThreshold(3)
        .withHttp5xxPercentThreshold(85)
        .withHttp5xxThreshold(15)
        .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_502, 90))
        .withHttpStatusThreshold(HttpStatusThreshold(HttpStatus.HTTP_STATUS_502, 10))
        .withHttpTrafficThreshold(HttpTrafficThreshold(None, critical = Some(1000)))
        .withLogMessageThreshold("BLAH", 1)
        .withTotalHttpRequestThreshold(2000)

      val alertConfigBuilder = AlertConfigBuilder("service1")
        .withAverageCPUThreshold(50)
        .withContainerKillThreshold(2)
        .withErrorsLoggedThreshold(5)
        .withExceptionThreshold(3)
        .withHttp5xxPercentThreshold(85)
        .withHttp5xxThreshold(15)
        .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_502, 90))
        .withHttpStatusThreshold(HttpStatusThreshold(HttpStatus.HTTP_STATUS_502, 10))
        .withHttpTrafficThreshold(HttpTrafficThreshold(None, critical = Some(1000)))
        .withLogMessageThreshold("BLAH", 1)
        .withTotalHttpRequestThreshold(2000)

      val teamConfigs = teamAlertConfigBuilder.build()

      teamConfigs shouldBe Seq(alertConfigBuilder)
    }

    "return TeamAlertConfigBuilder with correct 5xxPercentThreshold" in {
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttp5xxPercentThreshold(19.2, severity = AlertSeverity.Warning)

      alertConfigBuilder.http5xxPercentThreshold shouldBe Some(Http5xxPercentThreshold(19.2, severity = AlertSeverity.Warning))
    }

    "return TeamAlertConfigBuilder with correct integrations" in {
      val integrations = Seq("a", "b")
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withIntegrations(integrations: _*)

      alertConfigBuilder.integrations shouldBe integrations
    }

    "return TeamAlertConfigBuilder with correct AbsolutePercentSplitThresholds" in {
      val percent    = 15.5
      val crossover  = 50
      val absolute   = 20
      val hysteresis = 1.2
      val spikes     = 2
      val filter     = "status:200"
      val severity   = AlertSeverity.Critical

      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttpAbsolutePercentSplitThreshold(HttpAbsolutePercentSplitThreshold(percent, crossover, absolute, hysteresis, spikes, filter, severity))
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        httpAbsolutePercentSplitThreshold = Some(List(YamlHttpAbsolutePercentSplitThresholdAlert(15.5, 50, 20, 1.2, 2, "status:200", "critical")))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct AbsolutePercentSplitDownstreamServiceThresholds" in {
      val percent    = 15.5
      val crossover  = 50
      val absolute   = 20
      val hysteresis = 1.2
      val spikes     = 2
      val filter     = "status:200"
      val target     = "something.invalid"
      val severity   = AlertSeverity.Critical

      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttpAbsolutePercentSplitDownstreamServiceThreshold(
          HttpAbsolutePercentSplitDownstreamServiceThreshold(percent, crossover, absolute, hysteresis, spikes, filter, target, severity))
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        httpAbsolutePercentSplitDownstreamServiceThreshold =
          Some(List(YamlHttpAbsolutePercentSplitDownstreamServiceThresholdAlert(15.5, 50, 20, 1.2, 2, "status:200", "something.invalid", "critical")))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct AbsolutePercentSplitDownstreamHodThresholds" in {
      val percent    = 15.5
      val crossover  = 50
      val absolute   = 20
      val hysteresis = 1.2
      val spikes     = 2
      val filter     = "status:200"
      val target     = "hod-endpoint"
      val severity   = AlertSeverity.Critical

      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttpAbsolutePercentSplitDownstreamHodThreshold(
          HttpAbsolutePercentSplitDownstreamHodThreshold(percent, crossover, absolute, hysteresis, spikes, filter, target, severity))
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        httpAbsolutePercentSplitDownstreamHodThreshold =
          Some(List(YamlHttpAbsolutePercentSplitDownstreamHodThresholdAlert(15.5, 50, 20, 1.2, 2, "status:200", "hod-endpoint", "critical")))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct Http5xxPercentThreshold" in {
      val threshold = 19.9
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttp5xxPercentThreshold(threshold)
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(threshold, 0, "critical"))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct ExceptionThreshold" in {
      val threshold = 13
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withExceptionThreshold(threshold)
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(threshold, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical"))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "return TeamAlertConfigBuilder with disabled ErrorsLoggedThreshold" in {
      val threshold = 13
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withErrorsLoggedThreshold(threshold)
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        errorsLoggedThreshold = Some(YamlErrorsLoggedThresholdAlert(threshold))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct ContainerKillThreshold" in {
      val threshold = 2
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withContainerKillThreshold(threshold)
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(threshold)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical"))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct AverageCPUThreshold" in {
      val threshold = 67
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withAverageCPUThreshold(threshold)
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        averageCPUThreshold = Some(YamlAverageCPUThresholdAlert(threshold))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct http5xxThresholdSeverities" in {
      val threshold = 19
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttp5xxThreshold(threshold, AlertSeverity.Critical)
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        http5xxThreshold = Some(YamlHttp5xxThresholdAlert(threshold, "critical"))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct http90PercentileResponseTimeThreshold" in {
      val threshold = Http90PercentileResponseTimeThreshold(Some(10), Some(5), timePeriod = 10)
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttp90PercentileResponseTimeThreshold(threshold)
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        http90PercentileResponseTimeThreshold = Some(
          Seq(YamlHttp90PercentileResponseTimeThresholdAlert(10, 10, "warning"), YamlHttp90PercentileResponseTimeThresholdAlert(10, 5, "critical")))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "throw an exception if http90PercentileResponseTimeThreshold timePeriod is not valid" in {
      an[Exception] should be thrownBy
        TeamAlertConfigBuilder
          .teamAlerts(Seq())
          .withHttp90PercentileResponseTimeThreshold(
            Http90PercentileResponseTimeThreshold(Some(10), Some(5), timePeriod = 45)
          )
    }

    "return TeamAlertConfigBuilder with correct httpTrafficThreshold" in {
      val threshold = HttpTrafficThreshold(Some(10), Some(5), 35)
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttpTrafficThreshold(threshold)
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        httpTrafficThresholds = Some(Seq(YamlHttpTrafficThresholdAlert(10, 35, "warning"), YamlHttpTrafficThresholdAlert(5, 35, "critical")))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "throw exception if httpTrafficThreshold is defined multiple times" in {
      an[Exception] should be thrownBy TeamAlertConfigBuilder
        .teamAlerts(Seq())
        .withHttpTrafficThreshold(HttpTrafficThreshold(Some(10), Some(5), 35))
        .withHttpTrafficThreshold(HttpTrafficThreshold(Some(10), Some(5), 35))
    }

    "return TeamAlertConfigBuilder with correct httpStatusThresholds" in {
      val threshold1 = HttpStatusThreshold(HttpStatus.HTTP_STATUS_500, 19, AlertSeverity.Warning, HttpMethod.Post)
      val threshold2 = HttpStatusThreshold(HttpStatus.HTTP_STATUS_501, 20)
      val threshold3 = HttpStatusThreshold(HttpStatus.HTTP_STATUS(555), 55)
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttpStatusThreshold(threshold1)
        .withHttpStatusThreshold(threshold2)
        .withHttpStatusThreshold(threshold3)
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        httpStatusThresholds = Some(
          Seq(
            YamlHttpStatusThresholdAlert(19, "POST", 500, "warning"),
            YamlHttpStatusThresholdAlert(20, "ALL_METHODS", 501, "critical"),
            YamlHttpStatusThresholdAlert(55, "ALL_METHODS", 555, "critical")
          ))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct httpStatusPercentThresholds" in {
      val threshold1 = HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_500, 19.1, AlertSeverity.Warning, HttpMethod.Post)
      val threshold2 = HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_501, 20)
      val threshold3 = HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS(555), 55.5)
      val threshold4 = HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_502, 10)
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttpStatusPercentThreshold(threshold1)
        .withHttpStatusPercentThreshold(threshold2)
        .withHttpStatusPercentThreshold(threshold3)
        .withHttpStatusPercentThreshold(threshold4)
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder]            = alertConfigBuilder
        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        httpStatusPercentThresholds = Some(
          Seq(
            YamlHttpStatusPercentThresholdAlert(19.1, "POST", 500, "warning"),
            YamlHttpStatusPercentThresholdAlert(20.0, "ALL_METHODS", 501, "critical"),
            YamlHttpStatusPercentThresholdAlert(55.5, "ALL_METHODS", 555, "critical"),
            YamlHttpStatusPercentThresholdAlert(10.0, "ALL_METHODS", 502, "critical")
          ))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct totalHttpRequestThreshold" in {
      val requestThreshold = 35
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withTotalHttpRequestThreshold(requestThreshold)
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        totalHttpRequestThreshold = Some(YamlTotalHttpRequestThresholdAlert(requestThreshold))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct withHttp5xxPercentThreshold using optional minimum 5xx count" in {
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttp5xxPercentThreshold(12.2, minimumHttp5xxCountThreshold = 15, severity = AlertSeverity.Warning)
        .withIntegrations(integration)

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(12.2, 15, "warning"))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct logMessageThresholds" in {
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withLogMessageThreshold("SIMULATED_ERROR1", 19)
        .withLogMessageThreshold("SIMULATED_ERROR2", 20, lessThanMode = true)
        .withLogMessageThreshold("SIMULATED_ERROR3", 21, lessThanMode = true, AlertSeverity.Warning)
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = Some(YamlExceptionThresholdAlert(2, "critical")),
        http5xxPercentThreshold = Some(YamlHttp5xxPercentThresholdAlert(100.0, 0, "critical")),
        logMessageThresholds = Some(
          Seq(
            YamlLogMessageThresholdAlert(19, lessThanMode = false, "SIMULATED_ERROR1", "critical"),
            YamlLogMessageThresholdAlert(20, lessThanMode = true, "SIMULATED_ERROR2", "critical"),
            YamlLogMessageThresholdAlert(21, lessThanMode = true, "SIMULATED_ERROR3", "warning")
          ))
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "return TeamAlertConfigBuilder for platform services with correct containerKillThresholds" in {
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("ingress-gateway-public", "ingress-gateway-public-rate"))
        .isPlatformService(true)
        .withContainerKillThreshold(1)
        .withExceptionThreshold(Int.MaxValue) // disabled
        .withHttp5xxPercentThreshold(150)     // disabled
        .withIntegrations(integration)

      alertConfigBuilder.services shouldBe Seq("ingress-gateway-public", "ingress-gateway-public-rate")

      val fakeConfig = new AlertConfig {
        override def alertConfig: Seq[AlertConfigBuilder] = alertConfigBuilder

        override def environmentConfig: Seq[EnvironmentAlertBuilder] = envConfig
      }

      val output = AlertsYamlBuilder.convert(Seq(fakeConfig), Environment.Production)

      output.length shouldBe 2
      val service1Config = output(0)
      val service2Config = output(1)

      val expected = Alerts(
        containerKillThreshold = Some(YamlContainerKillThresholdAlert(1)),
        exceptionThreshold = None,
        http5xxPercentThreshold = None
      )

      service1Config.alerts shouldBe expected
      service2Config.alerts shouldBe expected
    }

    "throw exception if no service provided" in {
      an[RuntimeException] should be thrownBy TeamAlertConfigBuilder.teamAlerts(Seq())
    }

  }

}
