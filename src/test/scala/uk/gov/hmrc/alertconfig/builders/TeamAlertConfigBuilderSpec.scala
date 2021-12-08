/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.alertconfig.builders

import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import spray.json.{JsArray, JsString}
import uk.gov.hmrc.alertconfig._
import spray.json._
import uk.gov.hmrc.alertconfig.HttpStatus.HTTP_STATUS


class TeamAlertConfigBuilderSpec extends WordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach() {
    System.setProperty("app-config-path", "src/test/resources/app-config")
    System.setProperty("zone-mapping-path", "src/test/resources/zone-to-service-domain-mapping.yml")
  }

  "teamAlerts" should {
    "return TeamAlertConfigBuilder with correct default values" in {
      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      alertConfigBuilder.handlers shouldBe Seq("noop")
      alertConfigBuilder.http5xxPercentThreshold shouldBe 100
      alertConfigBuilder.http5xxThreshold shouldBe Http5xxThreshold(Int.MaxValue,AlertSeverity.critical)
      alertConfigBuilder.totalHttpRequestThreshold shouldBe Int.MaxValue
      alertConfigBuilder.exceptionThreshold shouldBe 2
      alertConfigBuilder.containerKillThreshold shouldBe 1
      alertConfigBuilder.averageCPUThreshold shouldBe Int.MaxValue
      alertConfigBuilder.httpStatusThresholds shouldBe List()
      alertConfigBuilder.logMessageThresholds shouldBe List()
      alertConfigBuilder.httpAbsolutePercentSplitThresholds shouldBe List()
    }

    "return TeamAlertConfigBuilder with correct handlers" in {
      val handlers = Seq("a", "b")
      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))
        .withHandlers(handlers:_*)

      alertConfigBuilder.handlers shouldBe handlers
    }

    "return TeamAlertConfigBuilder with correct AbsolutePercentSplitThresholds" in {
      val percent = 15.5
      val crossover = 50
      val absolute = 20
      val hysteresis = 1.2
      val spikes = 2
      val filter = "status:200"
      val severity = AlertSeverity.info

      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))
        .withHttpAbsolutePercentSplitThreshold(HttpAbsolutePercentSplitThreshold(percent, crossover, absolute, hysteresis, spikes, filter, severity))

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsArray(JsObject("absoluteThreshold" -> JsNumber(absolute),
        "crossOver" -> JsNumber(crossover),
        "errorFilter" -> JsString(filter),
        "excludeSpikes" -> JsNumber(spikes),
        "hysteresis" -> JsNumber(hysteresis),
        "percentThreshold" -> JsNumber(percent),
        "severity" -> JsString(severity.toString)))

      service1Config("absolute-percentage-split-threshold") shouldBe expected
      service2Config("absolute-percentage-split-threshold") shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct AbsolutePercentSplitDownstreamServiceThresholds" in {
      val percent = 15.5
      val crossover = 50
      val absolute = 20
      val hysteresis = 1.2
      val spikes = 2
      val filter = "status:200"
      val target = "something.invalid"
      val severity = AlertSeverity.info

      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))
        .withHttpAbsolutePercentSplitDownstreamServiceThreshold(HttpAbsolutePercentSplitDownstreamServiceThreshold(percent, crossover, absolute, hysteresis, spikes, filter, target, severity))

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsArray(JsObject("absoluteThreshold" -> JsNumber(absolute),
        "crossOver" -> JsNumber(crossover),
        "errorFilter" -> JsString(filter),
        "target" -> JsString(target),
        "excludeSpikes" -> JsNumber(spikes),
        "hysteresis" -> JsNumber(hysteresis),
        "percentThreshold" -> JsNumber(percent),
        "severity" -> JsString(severity.toString)))

      service1Config("absolute-percentage-split-downstream-service-threshold") shouldBe expected
      service2Config("absolute-percentage-split-downstream-service-threshold") shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct AbsolutePercentSplitDownstreamHodThresholds" in {
      val percent = 15.5
      val crossover = 50
      val absolute = 20
      val hysteresis = 1.2
      val spikes = 2
      val filter = "status:200"
      val target = "hod-endpoint"
      val severity = AlertSeverity.info

      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))
        .withHttpAbsolutePercentSplitDownstreamHodThreshold(HttpAbsolutePercentSplitDownstreamHodThreshold(percent, crossover, absolute, hysteresis, spikes, filter, target, severity))

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsArray(JsObject("absoluteThreshold" -> JsNumber(absolute),
        "crossOver" -> JsNumber(crossover),
        "errorFilter" -> JsString(filter),
        "target" -> JsString(target),
        "excludeSpikes" -> JsNumber(spikes),
        "hysteresis" -> JsNumber(hysteresis),
        "percentThreshold" -> JsNumber(percent),
        "severity" -> JsString(severity.toString)))

      service1Config("absolute-percentage-split-downstream-hod-threshold") shouldBe expected
      service2Config("absolute-percentage-split-downstream-hod-threshold") shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct Http5xxPercentThreshold" in {
      val threshold = 19.9
      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))
        .withHttp5xxPercentThreshold(threshold)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("5xx-percent-threshold") shouldBe JsNumber(threshold)
      service2Config("5xx-percent-threshold") shouldBe JsNumber(threshold)
    }

    "return TeamAlertConfigBuilder with correct ExceptionThreshold" in {
      val threshold = 13
      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))
        .withExceptionThreshold(threshold)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("exception-threshold") shouldBe JsNumber(threshold)
      service2Config("exception-threshold") shouldBe JsNumber(threshold)
    }

    "return TeamAlertConfigBuilder with correct AverageCPUThreshold" in {
      val threshold = 67
      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))
        .withAverageCPUThreshold(threshold)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("average-cpu-threshold") shouldBe JsNumber(threshold)
      service2Config("average-cpu-threshold") shouldBe JsNumber(threshold)
    }

    "return TeamAlertConfigBuilder with correct http5xxThresholdSeverities" in {
      val threshold = 19
      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))
        .withHttp5xxThreshold(threshold, AlertSeverity.warning)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsObject(
        "count" -> JsNumber(threshold),
        "severity" -> JsString("warning"))

      service1Config("5xx-threshold") shouldBe expected
      service2Config("5xx-threshold") shouldBe expected
    }

    "return TeamAlertConfigBuilder with correct httpStatusThresholds" in {
      val threshold1 = HttpStatusThreshold(HttpStatus.HTTP_STATUS_500, 19, AlertSeverity.warning, HttpMethod.post)
      val threshold2 = HttpStatusThreshold(HttpStatus.HTTP_STATUS_501, 20)
      val threshold3 = HttpStatusThreshold(HTTP_STATUS(555), 55)
      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))
        .withHttpStatusThreshold(threshold1)
        .withHttpStatusThreshold(threshold2)
        .withHttpStatusThreshold(threshold3)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsArray(
        JsObject("httpStatus" -> JsNumber(500),
          "count" -> JsNumber(19),
          "severity" -> JsString("warning"),
          "httpMethod" -> JsString("POST")),
        JsObject("httpStatus" -> JsNumber(501),
          "count" -> JsNumber(20),
          "severity" -> JsString("critical"),
          "httpMethod" -> JsString("*")),
        JsObject("httpStatus" -> JsNumber(555),
          "count" -> JsNumber(55),
          "severity" -> JsString("critical"),
          "httpMethod" -> JsString("*"))
      )

      service1Config("httpStatusThresholds") shouldBe expected
      service2Config("httpStatusThresholds") shouldBe expected
    }


    "build alert-config with correct allRequestThreshold" in {
      val requestThreshold = 35
      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))
        .withTotalHttpRequestsCountThreshold(requestThreshold)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config: Map[String, JsValue] = configs(0)
      val service2Config: Map[String, JsValue] = configs(1)

      service1Config("total-http-request-threshold") shouldBe JsNumber(requestThreshold)
      service2Config("total-http-request-threshold") shouldBe JsNumber(requestThreshold)
    }

    "return TeamAlertConfigBuilder with correct logMessageThresholds" in {
      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))
        .withLogMessageThreshold("SIMULATED_ERROR1", 19, lessThanMode = false)
        .withLogMessageThreshold("SIMULATED_ERROR2", 20, lessThanMode = true)

      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      val expected = JsArray(
        JsObject("message" -> JsString("SIMULATED_ERROR1"),
          "count" -> JsNumber(19), "lessThanMode" -> JsFalse),
        JsObject("message" -> JsString("SIMULATED_ERROR2"),
          "count" -> JsNumber(20), "lessThanMode" -> JsTrue)
      )

      service1Config("log-message-thresholds") shouldBe expected
      service2Config("log-message-thresholds") shouldBe expected
    }

    "return TeamAlertConfigBuilder for platform services with correct containerKillThresholds" in {
      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("ingress-gateway-public", "ingress-gateway-public-rate"))
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
      an [RuntimeException] should be thrownBy TeamAlertConfigBuilder.teamAlerts(Seq())
    }
  }



}
