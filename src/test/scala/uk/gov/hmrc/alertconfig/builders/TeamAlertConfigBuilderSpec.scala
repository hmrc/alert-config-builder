/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.alertconfig.{AlertSeverity, Http5xxThreshold, HttpStatus, HttpStatusThreshold, LogMessageThreshold}
import spray.json._


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
    }

    "return TeamAlertConfigBuilder with correct http5xxThresholdSeverities" in {

      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))
        .withHttp5xxThreshold(19, AlertSeverity.warning)


      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("5xx-threshold") shouldBe
        JsObject(
          "count" -> JsNumber(19),
          "severity" -> JsString("warning"))

      service2Config("5xx-threshold") shouldBe
        JsObject(
          "count" -> JsNumber(19),
          "severity" -> JsString("warning"))

    }


    "return TeamAlertConfigBuilder with correct httpStatusThresholds" in {

      val threshold1 = HttpStatusThreshold(HttpStatus.HTTP_STATUS_500, 19, AlertSeverity.warning)
      val threshold2 = HttpStatusThreshold(HttpStatus.HTTP_STATUS_501, 20)
      val alertConfigBuilder = TeamAlertConfigBuilder.teamAlerts(Seq("service1", "service2"))
        .withHttpStatusThreshold(threshold1)
        .withHttpStatusThreshold(threshold2)


      alertConfigBuilder.services shouldBe Seq("service1", "service2")
      val configs = alertConfigBuilder.build.map(_.build.get.parseJson.asJsObject.fields)

      configs.size shouldBe 2
      val service1Config = configs(0)
      val service2Config = configs(1)

      service1Config("httpStatusThresholds") shouldBe
        JsArray(
          JsObject("httpStatus" -> JsNumber(500),
            "count" -> JsNumber(19),
            "severity" -> JsString("warning")),
          JsObject("httpStatus" -> JsNumber(501),
            "count" -> JsNumber(20),
            "severity" -> JsString("critical"))
        )
      service2Config("httpStatusThresholds") shouldBe
        JsArray(
          JsObject("httpStatus" -> JsNumber(500),
            "count" -> JsNumber(19),
            "severity" -> JsString("warning")),
          JsObject("httpStatus" -> JsNumber(501),
            "count" -> JsNumber(20),
            "severity" -> JsString("critical"))
        )

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

      service1Config("log-message-thresholds") shouldBe
        JsArray(
          JsObject("message" -> JsString("SIMULATED_ERROR1"),
            "count" -> JsNumber(19), "lessThanMode" -> JsFalse),
          JsObject("message" -> JsString("SIMULATED_ERROR2"),
            "count" -> JsNumber(20), "lessThanMode" -> JsTrue)
        )
      service2Config("log-message-thresholds") shouldBe
        JsArray(
          JsObject("message" -> JsString("SIMULATED_ERROR1"),
            "count" -> JsNumber(19), "lessThanMode" -> JsFalse),
          JsObject("message" -> JsString("SIMULATED_ERROR2"),
            "count" -> JsNumber(20), "lessThanMode" -> JsTrue)
        )
    }


    "throw exception if no service provided" in {

      an [RuntimeException] should be thrownBy TeamAlertConfigBuilder.teamAlerts(Seq())


    }
  }



}
