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

import java.io.FileNotFoundException

import org.scalatest._
import spray.json._
import uk.gov.hmrc.alertconfig.HttpStatus._
import uk.gov.hmrc.alertconfig.{AlertSeverity, Http5xxThreshold, HttpAbsolutePercentSplitThreshold, HttpStatusThreshold}

class AlertConfigBuilderSpec extends WordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach() {
    System.setProperty("app-config-path", "src/test/resources/app-config")
    System.setProperty("zone-mapping-path", "src/test/resources/zone-to-service-domain-mapping.yml")
  }

  "AlertConfigBuilder" should {
    "build correct config" in {

      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withContainerKillThreshold(56).build.get.parseJson.asJsObject.fields

      config("app") shouldBe JsString("service1.domain.zone.1")
      config("handlers") shouldBe JsArray(JsString("h1"), JsString("h2"))
      config("exception-threshold") shouldBe JsNumber(2)
      config("5xx-threshold") shouldBe JsObject("count" -> JsNumber(Int.MaxValue), "severity" -> JsString("critical"))
      config("5xx-percent-threshold") shouldBe JsNumber(100)
      config("total-http-request-threshold") shouldBe JsNumber(Int.MaxValue)
      config("containerKillThreshold") shouldBe JsNumber(56)
      config("average-cpu-threshold") shouldBe JsNumber(Int.MaxValue)
      config("httpStatusThresholds") shouldBe JsArray()
      config("log-message-thresholds") shouldBe JsArray()
      config("absolute-percentage-split-threshold") shouldBe JsArray()
    }

    "build correct config for platform service" in {
      val platformServiceConfig = AlertConfigBuilder("ingress-gateway", handlers = Seq("h1","h2"))
        .isPlatformService(true)
        .withContainerKillThreshold(1)
        .build.get.parseJson.asJsObject.fields

      platformServiceConfig("app") shouldBe JsString("ingress-gateway.")
      platformServiceConfig("containerKillThreshold") shouldBe JsNumber(1)
    }

    "throw exception and stop processing when app config directory not found" in {
      System.setProperty("app-config-path", "this-directory-does-not-exist")

      intercept[FileNotFoundException] {
        AlertConfigBuilder("service1", handlers = Seq("h1", "h2")).build.get.parseJson.asJsObject.fields
      }
    }

    "Returns None when app config file not found" in {
      AlertConfigBuilder("absent-service", handlers = Seq("h1", "h2")).build shouldBe None
    }

    "Returns None when app config file exists but zone key is absent" in {
      AlertConfigBuilder("service-with-absent-zone-key", handlers = Seq("h1", "h2")).build shouldBe None
    }

    "Returns None when app config file exists but it unparsable" in {
      AlertConfigBuilder("service-with-unparseable-app-config", handlers = Seq("h1", "h2")).build shouldBe None
    }

    "Maps the correct service domain" in {
      val service2Config = AlertConfigBuilder("service2", handlers = Seq("h1", "h2")).build.get.parseJson.asJsObject.fields
      val service3Config = AlertConfigBuilder("service3", handlers = Seq("h1", "h2")).build.get.parseJson.asJsObject.fields
      service2Config("app") shouldBe JsString("service2.domain.zone.2")
      service3Config("app") shouldBe JsString("service3.domain.zone.3")
    }

    // Ignored as it cannot be run as part of the entire suite due to the system property setting.
    "throw exception and stop processing when zone to service domain mapping file not found" ignore {
      System.setProperty("zone-mapping-path", "this-file-does-not-exist")

      val exception = intercept[ExceptionInInitializerError] {
        val c = ZoneToServiceDomainMapper.getClass.getConstructor()
        c.setAccessible(true)
        c.newInstance()
      }
      assert(exception.getCause.isInstanceOf[FileNotFoundException])
    }

    "build/configure http status threshold with given thresholds and severities" in {
      val serviceConfig = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_502, 2, AlertSeverity.warning))
        .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_503, 3, AlertSeverity.error))
        .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_504, 4)).build.get.parseJson.asJsObject.fields

      serviceConfig("httpStatusThresholds") shouldBe JsArray(
        JsObject("httpStatus" -> JsNumber(502), "count" -> JsNumber(2), "severity" -> JsString("warning")),
        JsObject("httpStatus" -> JsNumber(503), "count" -> JsNumber(3), "severity" -> JsString("error")),
        JsObject("httpStatus" -> JsNumber(504), "count" -> JsNumber(4), "severity" -> JsString("critical"))
      )
    }

    "build/configure http status threshold with given generic threshold" in {
      val serviceConfig = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS(404))).build.get.parseJson.asJsObject.fields

      serviceConfig("httpStatusThresholds") shouldBe JsArray(
        JsObject("httpStatus" -> JsNumber(404), "count" -> JsNumber(1), "severity" -> JsString("critical"))
      )
    }

    "build/configure http 5xx threshold severity with given thresholds and severities" in {
      val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withHttp5xxThreshold(2, AlertSeverity.warning).build.get.parseJson.asJsObject.fields

      serviceConfig("5xx-threshold") shouldBe JsObject("count" -> JsNumber(2), "severity" -> JsString("warning"))
    }

    "build/configure http 5xx threshold severity with given thresholds and unspecified severity" in {
      val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withHttp5xxThreshold(2).build.get.parseJson.asJsObject.fields

      serviceConfig("5xx-threshold") shouldBe JsObject("count" -> JsNumber(2), "severity" -> JsString("critical"))
    }


    "build/configure logMessageThresholds with given thresholds" in {
      val serviceConfig = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withLogMessageThreshold("SIMULATED_ERROR1", 3)
        .withLogMessageThreshold("SIMULATED_ERROR2", 4, lessThanMode = false)
        .withLogMessageThreshold("SIMULATED_ERROR3", 5, lessThanMode = true).build.get.parseJson.asJsObject.fields

      serviceConfig("log-message-thresholds") shouldBe JsArray(
        JsObject("message" -> JsString("SIMULATED_ERROR1"), "count" -> JsNumber(3), "lessThanMode" -> JsFalse),
        JsObject("message" -> JsString("SIMULATED_ERROR2"), "count" -> JsNumber(4), "lessThanMode" -> JsFalse),
        JsObject("message" -> JsString("SIMULATED_ERROR3"), "count" -> JsNumber(5), "lessThanMode" -> JsTrue)
      )
    }

    "build/configure any empty http status threshold" in {
      val serviceConfig = AlertConfigBuilder("service1").build.get.parseJson.asJsObject.fields

      serviceConfig("httpStatusThresholds") shouldBe JsArray()
    }

    "build/configure HttpAbsolutePercentSplitThreshold with default parameters" in {
      val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withHttpAbsolutePercentSplitThreshold(HttpAbsolutePercentSplitThreshold()).build.get.parseJson.asJsObject.fields

      val expected = JsArray(JsObject("errorFilter" -> JsString("status:>498"),
        "absoluteThreshold" -> JsNumber(Int.MaxValue),
        "crossOver" -> JsNumber(0),
        "excludeSpikes" -> JsNumber(0),
        "hysteresis" -> JsNumber(1.0),
        "percentThreshold" -> JsNumber(100.0),
        "severity" -> JsString("critical")))

      serviceConfig("absolute-percentage-split-threshold") shouldBe expected
    }
  }

  "build/configure HttpAbsolutePercentSplitThreshold with required parameters" in {
    val percent = 10.2
    val crossOver = 20
    val absolute = 30
    val hysteresis = 1.2
    val excludeSpikes = 2
    val filter = "Some error"
    val severity = AlertSeverity.warning

    val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
      .withHttpAbsolutePercentSplitThreshold(HttpAbsolutePercentSplitThreshold(
        percent, crossOver, absolute, hysteresis, excludeSpikes, filter, severity)).build.get.parseJson.asJsObject.fields

    val expected = JsArray(JsObject("errorFilter" -> JsString(filter),
      "absoluteThreshold" -> JsNumber(absolute),
      "crossOver" -> JsNumber(crossOver),
      "excludeSpikes" -> JsNumber(excludeSpikes),
      "hysteresis" -> JsNumber(hysteresis),
      "percentThreshold" -> JsNumber(percent),
      "severity" -> JsString(severity.toString)))

    serviceConfig("absolute-percentage-split-threshold") shouldBe expected
  }

  "return config with correct handlers" in {
    val handlers = Seq("a", "b")
    val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
      .withHandlers(handlers:_*).build.get.parseJson.asJsObject.fields

    val expected = JsArray(handlers.map(JsString(_)).toVector)
    serviceConfig("handlers") shouldBe expected
  }

  "build/configure ExceptionThreshold with required parameters" in {
    val threshold = 12
    val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
      .withExceptionThreshold(threshold).build.get.parseJson.asJsObject.fields

    serviceConfig("exception-threshold") shouldBe JsNumber(threshold)
  }

  "build/configure http5xxPercentThreshold with required parameters" in {
    val threshold = 13.3
    val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
      .withHttp5xxPercentThreshold(threshold).build.get.parseJson.asJsObject.fields

    serviceConfig("5xx-percent-threshold") shouldBe JsNumber(threshold)
  }

  "build/configure averageCPUThreshold with required parameters" in {
    val threshold = 15
    val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
      .withAverageCPUThreshold(threshold).build.get.parseJson.asJsObject.fields

    serviceConfig("average-cpu-threshold") shouldBe JsNumber(threshold)
  }
}
