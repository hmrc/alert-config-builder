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
import spray.json.{JsArray, JsString, JsObject}
import spray.json._

class TeamAlertConfigBuilderSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    System.setProperty("app-config-path", "src/test/resources/app-config")
    System.setProperty("zone-mapping-path", "src/test/resources/zone-to-service-domain-mapping.yml")
  }

  "teamAlerts" should {
    "return TeamAlertConfigBuilder with correct default values" in {
      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      alertConfigBuilder.handlers shouldBe Seq("noop")
      alertConfigBuilder.averageCPUThreshold shouldBe AverageCPUThreshold(Int.MaxValue, AlertingPlatform.Default)
      alertConfigBuilder.containerKillThreshold shouldBe ContainerKillThreshold(1, AlertingPlatform.Default)
      alertConfigBuilder.exceptionThreshold shouldBe ExceptionThreshold(2, AlertSeverity.Critical)
      alertConfigBuilder.http5xxPercentThreshold shouldBe Http5xxPercentThreshold(100, severity = AlertSeverity.Critical)
      alertConfigBuilder.http5xxThreshold shouldBe Http5xxThreshold(Int.MaxValue, AlertSeverity.Critical)
      alertConfigBuilder.http90PercentileResponseTimeThresholds shouldBe List()
      alertConfigBuilder.httpAbsolutePercentSplitThresholds shouldBe List()
      alertConfigBuilder.httpStatusThresholds shouldBe List()
      alertConfigBuilder.httpTrafficThresholds shouldBe List()
      alertConfigBuilder.logMessageThresholds shouldBe List()
      alertConfigBuilder.totalHttpRequestThreshold shouldBe TotalHttpRequestThreshold(Int.MaxValue, AlertingPlatform.Default)
    }

    "result in identical defaults to AlertConfigBuilder" in {
      val teamAlertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1"))
      val alertConfigBuilder = AlertConfigBuilder("service1")

      val teamConfigs = teamAlertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)
      val configs = alertConfigBuilder.build.get.parseJson.asJsObject.fields

      teamConfigs shouldBe List(configs)
    }

    "result in identical config to AlertConfigBuilder" in {
      val teamAlertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1"
        )).withAverageCPUThreshold(50)
        .withContainerKillThreshold(2)
        .withErrorsLoggedThreshold(5)
        .withExceptionThreshold(3)
        .withHttp5xxPercentThreshold(85)
        .withHttp5xxThreshold(15)
        .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_502, 90))
        .withHttpStatusThreshold(HttpStatusThreshold(HttpStatus.HTTP_STATUS_502, 10))
        .withHttpTrafficThreshold(HttpTrafficThreshold(None, critical=Some(1000)))
        .withLogMessageThreshold("BLAH", 1)
        .withTotalHttpRequestsCountThreshold(2000)

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
        .withTotalHttpRequestsCountThreshold(2000)

      val teamConfigs = teamAlertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)
      val configs = alertConfigBuilder.build.get.parseJson.asJsObject.fields

      teamConfigs shouldBe List(configs)
    }

    "result in identical config to AlertConfigBuilder in migrated environment" in {
      EnvironmentVars.setEnv("ENVIRONMENT", "integration")

      val teamAlertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1"
        )).withAverageCPUThreshold(50)
        .withContainerKillThreshold(2)
        .withErrorsLoggedThreshold(5)
        .withExceptionThreshold(3)
        .withHttp5xxPercentThreshold(85)
        .withHttp5xxThreshold(15)
        .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_502, 90))
        .withHttpStatusThreshold(HttpStatusThreshold(HttpStatus.HTTP_STATUS_502, 10))
        .withHttpTrafficThreshold(HttpTrafficThreshold(None, critical = Some(1000)))
        .withLogMessageThreshold("BLAH", 1)
        .withTotalHttpRequestsCountThreshold(2000)

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
        .withTotalHttpRequestsCountThreshold(2000)

      val teamConfigs = teamAlertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)
      val configs = alertConfigBuilder.build.get.parseJson.asJsObject.fields

      teamConfigs shouldBe List(configs)

      EnvironmentVars.unsetEnv("ENVIRONMENT")
    }

    "return TeamAlertConfigBuilder with correct 5xxPercentThreshold" in {
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttp5xxPercentThreshold(19.2, severity = AlertSeverity.Warning)

      alertConfigBuilder.http5xxPercentThreshold shouldBe Http5xxPercentThreshold(19.2, severity = AlertSeverity.Warning)
    }

    "return TeamAlertConfigBuilder with correct handlers" in {
      val handlers = Seq("a", "b")
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHandlers(handlers: _*)

      alertConfigBuilder.handlers shouldBe handlers
    }

    "return TeamAlertConfigBuilder with correct AbsolutePercentSplitThresholds" in {
      val percent    = 15.5
      val crossover  = 50
      val absolute   = 20
      val hysteresis = 1.2
      val spikes     = 2
      val filter     = "status:200"
      val severity   = AlertSeverity.Warning

      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttpAbsolutePercentSplitThreshold(HttpAbsolutePercentSplitThreshold(percent, crossover, absolute, hysteresis, spikes, filter, severity))

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsArray(
        JsObject(
          "absoluteThreshold" -> JsNumber(absolute),
          "crossOver"         -> JsNumber(crossover),
          "errorFilter"       -> JsString(filter),
          "excludeSpikes"     -> JsNumber(spikes),
          "hysteresis"        -> JsNumber(hysteresis),
          "percentThreshold"  -> JsNumber(percent),
          "severity"          -> JsString(severity.toString),
          "alertingPlatform"  -> JsString(AlertingPlatform.Default.toString)
      ))

      service1Config("absolute-percentage-split-threshold") shouldBe expected
      service2Config("absolute-percentage-split-threshold") shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct AbsolutePercentSplitDownstreamServiceThresholds" in {
      val percent    = 15.5
      val crossover  = 50
      val absolute   = 20
      val hysteresis = 1.2
      val spikes     = 2
      val filter     = "status:200"
      val target     = "something.invalid"
      val severity   = AlertSeverity.Warning

      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttpAbsolutePercentSplitDownstreamServiceThreshold(
          HttpAbsolutePercentSplitDownstreamServiceThreshold(percent, crossover, absolute, hysteresis, spikes, filter, target, severity))

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsArray(
        JsObject(
          "absoluteThreshold" -> JsNumber(absolute),
          "crossOver"         -> JsNumber(crossover),
          "errorFilter"       -> JsString(filter),
          "target"            -> JsString(target),
          "excludeSpikes"     -> JsNumber(spikes),
          "hysteresis"        -> JsNumber(hysteresis),
          "percentThreshold"  -> JsNumber(percent),
          "severity"          -> JsString(severity.toString)
        ))

      service1Config("absolute-percentage-split-downstream-service-threshold") shouldBe expected
      service2Config("absolute-percentage-split-downstream-service-threshold") shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct AbsolutePercentSplitDownstreamHodThresholds" in {
      val percent    = 15.5
      val crossover  = 50
      val absolute   = 20
      val hysteresis = 1.2
      val spikes     = 2
      val filter     = "status:200"
      val target     = "hod-endpoint"
      val severity   = AlertSeverity.Warning

      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttpAbsolutePercentSplitDownstreamHodThreshold(
          HttpAbsolutePercentSplitDownstreamHodThreshold(percent, crossover, absolute, hysteresis, spikes, filter, target, severity))

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsArray(
        JsObject(
          "absoluteThreshold" -> JsNumber(absolute),
          "crossOver"         -> JsNumber(crossover),
          "errorFilter"       -> JsString(filter),
          "target"            -> JsString(target),
          "excludeSpikes"     -> JsNumber(spikes),
          "hysteresis"        -> JsNumber(hysteresis),
          "percentThreshold"  -> JsNumber(percent),
          "severity"          -> JsString(severity.toString)
        ))

      service1Config("absolute-percentage-split-downstream-hod-threshold") shouldBe expected
      service2Config("absolute-percentage-split-downstream-hod-threshold") shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct Http5xxPercentThreshold" in {
      val threshold = 19.9
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttp5xxPercentThreshold(threshold)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("5xx-percent-threshold") shouldBe JsObject(
        "severity"         -> JsString("critical"),
        "percentage"       -> JsNumber(threshold),
        "alertingPlatform" -> JsString(AlertingPlatform.Default.toString)
      )
      service2Config("5xx-percent-threshold") shouldBe JsObject(
        "severity"         -> JsString("critical"),
        "percentage"       -> JsNumber(threshold),
        "alertingPlatform" -> JsString(AlertingPlatform.Default.toString)
      )
    }

    "return TeamAlertConfigBuilder with disabled Http5xxPercentThreshold when alerting platform is Grafana" in {
      val threshold          = 19.9
      val disabledThreshold  = 333.33
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttp5xxPercentThreshold(threshold, alertingPlatform = AlertingPlatform.Grafana)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("5xx-percent-threshold") shouldBe JsObject(
        "severity"         -> JsString("critical"),
        "percentage"       -> JsNumber(disabledThreshold),
        "alertingPlatform" -> JsString(AlertingPlatform.Grafana.toString)
      )
      service2Config("5xx-percent-threshold") shouldBe JsObject(
        "severity"         -> JsString("critical"),
        "percentage"       -> JsNumber(disabledThreshold),
        "alertingPlatform" -> JsString(AlertingPlatform.Grafana.toString)
      )
    }

    "return TeamAlertConfigBuilder with correct ExceptionThreshold" in {
      val threshold = 13
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withExceptionThreshold(threshold, AlertSeverity.Warning)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("exception-threshold") shouldBe JsObject(
        "count"            -> JsNumber(threshold),
        "severity"         -> JsString("warning"),
        "alertingPlatform" -> JsString(AlertingPlatform.Default.toString)
      )
      service2Config("exception-threshold") shouldBe JsObject(
        "count"            -> JsNumber(threshold),
        "severity"         -> JsString("warning"),
        "alertingPlatform" -> JsString(AlertingPlatform.Default.toString)
      )
    }

    "return TeamAlertConfigBuilder with disabled ExceptionThreshold when alerting platform is Grafana" in {
      val threshold = 13
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withExceptionThreshold(threshold, AlertSeverity.Warning, AlertingPlatform.Grafana)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("exception-threshold") shouldBe JsObject(
        "count"            -> JsNumber(Int.MaxValue),
        "severity"         -> JsString("warning"),
        "alertingPlatform" -> JsString(AlertingPlatform.Grafana.toString)
      )
      service2Config("exception-threshold") shouldBe JsObject(
        "count"            -> JsNumber(Int.MaxValue),
        "severity"         -> JsString("warning"),
        "alertingPlatform" -> JsString(AlertingPlatform.Grafana.toString)
      )
    }

    "return TeamAlertConfigBuilder with correct ErrorsLoggedThreshold" in {
      val threshold = 13
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withErrorsLoggedThreshold(threshold)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("errors-logged-threshold") shouldBe JsNumber(threshold)
      service2Config("errors-logged-threshold") shouldBe JsNumber(threshold)
    }

    "return TeamAlertConfigBuilder with disabled ErrorsLoggedThreshold when alerting platform is Grafana" in {
      val threshold = 13
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withErrorsLoggedThreshold(threshold, AlertingPlatform.Grafana)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("errors-logged-threshold") shouldBe JsNumber(Int.MaxValue)
      service2Config("errors-logged-threshold") shouldBe JsNumber(Int.MaxValue)
    }

    "return TeamAlertConfigBuilder with default ContainerKillThreshold" in {
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("containerKillThreshold") shouldBe JsNumber(1)
      service2Config("containerKillThreshold") shouldBe JsNumber(1)
    }

    "return TeamAlertConfigBuilder with correct ContainerKillThreshold" in {
      val threshold = 2
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withContainerKillThreshold(threshold)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("containerKillThreshold") shouldBe JsNumber(threshold)
      service2Config("containerKillThreshold") shouldBe JsNumber(threshold)
    }

    "return TeamAlertConfigBuilder with disabled ContainerKillThreshold when alerting platform is Grafana" in {
      val threshold = 2
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withContainerKillThreshold(threshold, AlertingPlatform.Grafana)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("containerKillThreshold") shouldBe JsNumber(Int.MaxValue)
      service2Config("containerKillThreshold") shouldBe JsNumber(Int.MaxValue)
    }

    "return TeamAlertConfigBuilder with correct AverageCPUThreshold" in {
      val threshold = 67
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withAverageCPUThreshold(threshold)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("average-cpu-threshold") shouldBe JsNumber(threshold)
      service2Config("average-cpu-threshold") shouldBe JsNumber(threshold)
    }

    "return TeamAlertConfigBuilder with disabled AverageCPUThreshold when alerting plaform is Grafana" in {
      val threshold = 67
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withAverageCPUThreshold(threshold, alertingPlatform = AlertingPlatform.Grafana)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("average-cpu-threshold") shouldBe JsNumber(Int.MaxValue)
      service2Config("average-cpu-threshold") shouldBe JsNumber(Int.MaxValue)
    }

    "return TeamAlertConfigBuilder with correct http5xxThresholdSeverities" in {
      val threshold = 19
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttp5xxThreshold(threshold, AlertSeverity.Warning)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsObject("count" -> JsNumber(threshold), "severity" -> JsString("warning"), "alertingPlatform" -> JsString("Default"))

      service1Config("5xx-threshold") shouldBe expected
      service2Config("5xx-threshold") shouldBe expected
    }

    "return TeamAlertConfigBuilder with disabled http5xxThresholdSeverities when alerting platform is Grafana" in {
      val threshold = 19
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttp5xxThreshold(threshold, AlertSeverity.Warning, AlertingPlatform.Grafana)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsObject("count" -> JsNumber(Int.MaxValue), "severity" -> JsString("warning"), "alertingPlatform" -> JsString("Grafana"))

      service1Config("5xx-threshold") shouldBe expected
      service2Config("5xx-threshold") shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct http90PercentileResponseTimeThreshold" in {
      val threshold = Http90PercentileResponseTimeThreshold(Some(10), Some(5), timePeriod = 10)
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttp90PercentileResponseTimeThreshold(threshold)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsObject("alertingPlatform" -> JsString("Default"), "warning" -> JsNumber(10), "critical" -> JsNumber(5), "timePeriod" -> JsNumber(10))

      service1Config("http90PercentileResponseTimeThresholds") shouldBe expected
      service2Config("http90PercentileResponseTimeThresholds") shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct http90PercentileResponseTimeThreshold with alerting platform set to Grafana" in {
      val threshold = Http90PercentileResponseTimeThreshold(Some(10), Some(5), timePeriod = 10, alertingPlatform = AlertingPlatform.Grafana)
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttp90PercentileResponseTimeThreshold(threshold)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsObject("alertingPlatform" -> JsString("Grafana"), "warning" -> JsNumber(10), "critical" -> JsNumber(5), "timePeriod" -> JsNumber(10))

      service1Config("http90PercentileResponseTimeThresholds") shouldBe expected
      service2Config("http90PercentileResponseTimeThresholds") shouldBe expected
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

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsArray(
        JsObject(
          "warning"                  -> JsNumber(10),
          "critical"                 -> JsNumber(5),
          "maxMinutesBelowThreshold" -> JsNumber(35),
          "alertingPlatform"         -> JsString("Default")
        )
      )

      service1Config("httpTrafficThresholds") shouldBe expected
      service2Config("httpTrafficThresholds") shouldBe expected
    }

    "return TeamAlertConfigBuilder with empty httpTrafficThreshold when alerting platform is Grafana" in {
      val threshold = HttpTrafficThreshold(Some(10), Some(5), 35, AlertingPlatform.Grafana)
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttpTrafficThreshold(threshold)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsArray() // Should be no alert config for Sensu generated

      service1Config("httpTrafficThresholds") shouldBe expected
      service2Config("httpTrafficThresholds") shouldBe expected
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

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsArray(
        JsObject(
          "alertingPlatform" -> JsString("Default"),
          "httpStatus"       -> JsNumber(500),
          "count"            -> JsNumber(19),
          "severity"         -> JsString("warning"),
          "httpMethod"       -> JsString("POST")
        ),
        JsObject(
          "alertingPlatform" -> JsString("Default"),
          "httpStatus"       -> JsNumber(501),
          "count"            -> JsNumber(20),
          "severity"         -> JsString("critical"),
          "httpMethod"       -> JsString("ALL_METHODS")
        ),
        JsObject(
          "alertingPlatform" -> JsString("Default"),
          "httpStatus"       -> JsNumber(555),
          "count"            -> JsNumber(55),
          "severity"         -> JsString("critical"),
          "httpMethod"       -> JsString("ALL_METHODS")
        )
      )

      service1Config("httpStatusThresholds") shouldBe expected
      service2Config("httpStatusThresholds") shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct httpStatusPercentThresholds" in {
      val threshold1 = HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_500, 19.1, AlertSeverity.Warning, HttpMethod.Post)
      val threshold2 = HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_501, 20)
      val threshold3 = HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS(555), 55.5)
      // threshold4 should not appear in sensu (json) output as alerting platform is Grafana
      val threshold4 = HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_502, 10, alertingPlatform = AlertingPlatform.Grafana)
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withHttpStatusPercentThreshold(threshold1)
        .withHttpStatusPercentThreshold(threshold2)
        .withHttpStatusPercentThreshold(threshold3)
        .withHttpStatusPercentThreshold(threshold4)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsArray(
        JsObject(
          "httpStatus"       -> JsNumber(500),
          "percentage"       -> JsNumber(19.1),
          "severity"         -> JsString("warning"),
          "httpMethod"       -> JsString("POST"),
          "alertingPlatform" -> JsString(AlertingPlatform.Default.toString)),
        JsObject(
          "httpStatus"       -> JsNumber(501),
          "percentage"       -> JsNumber(20),
          "severity"         -> JsString("critical"),
          "httpMethod"       -> JsString("ALL_METHODS"),
          "alertingPlatform" -> JsString(AlertingPlatform.Default.toString)),
        JsObject(
          "httpStatus"       -> JsNumber(555),
          "percentage"       -> JsNumber(55.5),
          "severity"         -> JsString("critical"),
          "httpMethod"       -> JsString("ALL_METHODS"),
          "alertingPlatform" -> JsString(AlertingPlatform.Default.toString))
      )

      service1Config("httpStatusPercentThresholds") shouldBe expected
      service2Config("httpStatusPercentThresholds") shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct totalHttpRequestThreshold" in {
      val requestThreshold = 35
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withTotalHttpRequestsCountThreshold(requestThreshold, AlertingPlatform.Grafana)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config: Map[String, JsValue] = configs(0)
      val service2Config: Map[String, JsValue] = configs(1)

      service1Config("total-http-request-threshold") shouldBe JsNumber(Int.MaxValue)
      service2Config("total-http-request-threshold") shouldBe JsNumber(Int.MaxValue)
    }

    "return TeamAlertConfigBuilder with disabled totalHttpRequestThreshold when alerting platform is Grafana" in {
      val requestThreshold = 35
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withTotalHttpRequestsCountThreshold(requestThreshold, alertingPlatform = AlertingPlatform.Grafana)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config: Map[String, JsValue] = configs(0)
      val service2Config: Map[String, JsValue] = configs(1)

      service1Config("total-http-request-threshold") shouldBe JsNumber(Int.MaxValue)
      service2Config("total-http-request-threshold") shouldBe JsNumber(Int.MaxValue)
    }

    "return TeamAlertConfigBuilder with correct withHttp5xxPercentThreshold" in {
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1"))
        .withLogMessageThreshold("SIMULATED_ERROR1", 19, lessThanMode = false)
        .withLogMessageThreshold("SIMULATED_ERROR2", 20, lessThanMode = true)
        .withHttp5xxPercentThreshold(12.2, severity = AlertSeverity.Warning)

      alertConfigBuilder.services shouldBe Seq("service1")
      val configs                              = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)
      val service1Config: Map[String, JsValue] = configs(0)

      service1Config("5xx-percent-threshold") shouldBe JsObject(
        "percentage"       -> JsNumber(12.2),
        "severity"         -> JsString("warning"),
        "alertingPlatform" -> JsString(AlertingPlatform.Default.toString)
      )
    }

    "return TeamAlertConfigBuilder with disabled withHttp5xxPercentThreshold when alerting platform is Grafana" in {
      val threshold          = 19.9
      val disabledThreshold  = 333.33
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1"))
        .withLogMessageThreshold("SIMULATED_ERROR1", 19, lessThanMode = false)
        .withLogMessageThreshold("SIMULATED_ERROR2", 20, lessThanMode = true)
        .withHttp5xxPercentThreshold(threshold, severity = AlertSeverity.Warning, alertingPlatform = AlertingPlatform.Grafana)

      alertConfigBuilder.services shouldBe Seq("service1")
      val configs                              = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)
      val service1Config: Map[String, JsValue] = configs(0)

      service1Config("5xx-percent-threshold") shouldBe JsObject(
        "percentage"       -> JsNumber(disabledThreshold),
        "severity"         -> JsString("warning"),
        "alertingPlatform" -> JsString(AlertingPlatform.Grafana.toString)
      )
    }

    "return TeamAlertConfigBuilder with correct logMessageThresholds" in {
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("service1", "service2"))
        .withLogMessageThreshold("SIMULATED_ERROR1", 19, lessThanMode = false)
        .withLogMessageThreshold("SIMULATED_ERROR2", 20, lessThanMode = true)
        .withLogMessageThreshold("SIMULATED_ERROR3", 21, lessThanMode = true, AlertSeverity.Warning)
        .withLogMessageThreshold("SIMULATED_ERROR4", 22, alertingPlatform = AlertingPlatform.Grafana)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsArray(
        JsObject(
          "message"          -> JsString("SIMULATED_ERROR1"),
          "count"            -> JsNumber(19),
          "lessThanMode"     -> JsFalse,
          "severity"         -> JsString("critical"),
          "alertingPlatform" -> JsString(AlertingPlatform.Default.toString)
        ),
        JsObject(
          "message"          -> JsString("SIMULATED_ERROR2"),
          "count"            -> JsNumber(20),
          "lessThanMode"     -> JsTrue,
          "severity"         -> JsString("critical"),
          "alertingPlatform" -> JsString(AlertingPlatform.Default.toString)
        ),
        JsObject(
          "message"          -> JsString("SIMULATED_ERROR3"),
          "count"            -> JsNumber(21),
          "lessThanMode"     -> JsTrue,
          "severity"         -> JsString("warning"),
          "alertingPlatform" -> JsString(AlertingPlatform.Default.toString)
        )
      )

      service1Config("log-message-thresholds") shouldBe expected
      service2Config("log-message-thresholds") shouldBe expected
    }

    "return TeamAlertConfigBuilder for platform services with correct containerKillThresholds" in {
      val alertConfigBuilder = TeamAlertConfigBuilder
        .teamAlerts(Seq("ingress-gateway-public", "ingress-gateway-public-rate"))
        .isPlatformService(true)
        .withContainerKillThreshold(1)

      alertConfigBuilder.services shouldBe Seq("ingress-gateway-public", "ingress-gateway-public-rate")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("app") shouldBe JsString("ingress-gateway-public.")
      service1Config("containerKillThreshold") shouldBe JsNumber(1)
      service2Config("app") shouldBe JsString("ingress-gateway-public-rate.")
      service2Config("containerKillThreshold") shouldBe JsNumber(1)
    }

    "throw exception if no service provided" in {
      an[RuntimeException] should be thrownBy TeamAlertConfigBuilder.teamAlerts(Seq())
    }

  }

}
