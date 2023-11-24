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

import java.io.FileNotFoundException
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json._

class AlertConfigBuilderSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    System.setProperty("app-config-path", "src/test/resources/app-config")
    System.setProperty("zone-mapping-path", "src/test/resources/zone-to-service-domain-mapping.yml")
  }

  "AlertConfigBuilder" should {
    "build correct config" in {

      val config = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withContainerKillThreshold(56).build.get.parseJson.asJsObject.fields

      config("app") shouldBe JsString("service1.domain.zone.1")
      config("handlers") shouldBe JsArray(JsString("h1"), JsString("h2"))
      config("exception-threshold") shouldBe JsObject("count" -> JsNumber(2), "severity" -> JsString("critical"))
      config("5xx-threshold") shouldBe JsObject("count" -> JsNumber(Int.MaxValue), "severity" -> JsString("critical"))
      config("5xx-percent-threshold") shouldBe JsObject(
        "severity" -> JsString("critical"),
        "percentage" -> JsNumber(100)
      )
      config("total-http-request-threshold") shouldBe JsNumber(Int.MaxValue)
      config("containerKillThreshold") shouldBe JsNumber(56)
      config("average-cpu-threshold") shouldBe JsNumber(Int.MaxValue)
      config("httpStatusThresholds") shouldBe JsArray()
      config("httpStatusPercentThresholds") shouldBe JsArray()
      config("metricsThresholds") shouldBe JsArray()
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
        .withHttpStatusThreshold(HttpStatusThreshold(HttpStatus.HTTP_STATUS_502, 2, AlertSeverity.Warning, HttpMethod.Post))
        .withHttpStatusThreshold(HttpStatusThreshold(HttpStatus.HTTP_STATUS_504, 4)).build.get.parseJson.asJsObject.fields

      serviceConfig("httpStatusThresholds") shouldBe JsArray(
        JsObject("httpStatus" -> JsNumber(502), "count" -> JsNumber(2), "severity" -> JsString("warning"), "httpMethod" -> JsString("POST")),
        JsObject("httpStatus" -> JsNumber(504), "count" -> JsNumber(4), "severity" -> JsString("critical"), "httpMethod" -> JsString("ALL_METHODS"))
      )
    }

    "build/configure http status threshold with given generic threshold" in {
      val serviceConfig = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withHttpStatusThreshold(HttpStatusThreshold(HttpStatus.HTTP_STATUS(404))).build.get.parseJson.asJsObject.fields

      serviceConfig("httpStatusThresholds") shouldBe JsArray(
        JsObject("httpStatus" -> JsNumber(404), "count" -> JsNumber(1), "severity" -> JsString("critical"), "httpMethod" -> JsString("ALL_METHODS"))
      )
    }

    "build/configure http status percent threshold with given thresholds and severities" in {
      val serviceConfig = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_502, 2.2, AlertSeverity.Warning, HttpMethod.Post))
        .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS_504, 4.4)).build.get.parseJson.asJsObject.fields

      serviceConfig("httpStatusPercentThresholds") shouldBe JsArray(
        JsObject("httpStatus" -> JsNumber(502), "percentage" -> JsNumber(2.2), "severity" -> JsString("warning"), "httpMethod" -> JsString("POST")),
        JsObject("httpStatus" -> JsNumber(504), "percentage" -> JsNumber(4.4), "severity" -> JsString("critical"), "httpMethod" -> JsString("ALL_METHODS"))
      )
    }

    "build/configure http status percent threshold with given generic threshold" in {
      val serviceConfig = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HttpStatus.HTTP_STATUS(404))).build.get.parseJson.asJsObject.fields

      serviceConfig("httpStatusPercentThresholds") shouldBe JsArray(
        JsObject("httpStatus" -> JsNumber(404), "percentage" -> JsNumber(100.0), "severity" -> JsString("critical"), "httpMethod" -> JsString("ALL_METHODS"))
      )
    }

    "build/configure http 5xx threshold severity with given thresholds and severities" in {
      val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withHttp5xxThreshold(2, AlertSeverity.Warning).build.get.parseJson.asJsObject.fields

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
        .withLogMessageThreshold("SIMULATED_ERROR3", 5, lessThanMode = true)
        .withLogMessageThreshold("SIMULATED_ERROR4", 6, lessThanMode = true, AlertSeverity.Warning).build.get.parseJson.asJsObject.fields

      serviceConfig("log-message-thresholds") shouldBe JsArray(
        JsObject("message" -> JsString("SIMULATED_ERROR1"), "count" -> JsNumber(3), "lessThanMode" -> JsFalse, "severity" -> JsString("critical")),
        JsObject("message" -> JsString("SIMULATED_ERROR2"), "count" -> JsNumber(4), "lessThanMode" -> JsFalse, "severity" -> JsString("critical")),
        JsObject("message" -> JsString("SIMULATED_ERROR3"), "count" -> JsNumber(5), "lessThanMode" -> JsTrue, "severity" -> JsString("critical")),
        JsObject("message" -> JsString("SIMULATED_ERROR4"), "count" -> JsNumber(6), "lessThanMode" -> JsTrue, "severity" -> JsString("warning"))
      )
    }

    "return AlertConfigBuilder with correct httpTrafficThreshold" in {
      val threshold = HttpTrafficThreshold(Some(10), Some(5), 35)
      val serviceConfig = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withHttpTrafficThreshold(threshold).build.get.parseJson.asJsObject.fields

      serviceConfig("httpTrafficThresholds") shouldBe JsArray(
        JsObject(
          "warning" -> JsNumber(10),
          "critical" -> JsNumber(5),
          "maxMinutesBelowThreshold" -> JsNumber(35),
          "alertingPlatform" -> JsString("Sensu")
        )
      )
    }

    "throw exception if httpTrafficThreshold is defined multiple times" in {
      an[Exception] should be thrownBy AlertConfigBuilder("service1")
        .withHttpTrafficThreshold(HttpTrafficThreshold(Some(10), Some(5), 35))
        .withHttpTrafficThreshold(HttpTrafficThreshold(Some(10), Some(5), 35))
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

    "build/configure metrics rate increase" in {
      val serviceConfig = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withHttp5xxRateIncrease(Http5xxRateIncrease(name = "alert_custom_name_1", simple_threshold = 10, complex_threshold = 100))
        .withHttp5xxRateIncrease(Http5xxRateIncrease(name = "alert_custom_name_2", simple_threshold = 20, complex_threshold = 200))
        .build.get.parseJson.asJsObject.fields

      serviceConfig("http5xxRateIncrease") shouldBe JsArray(
        JsObject("name" -> JsString("alert_custom_name_1"), "simple_threshold" -> JsNumber(10), "complex_threshold" -> JsNumber(100)),
        JsObject("name" -> JsString("alert_custom_name_2"), "simple_threshold" -> JsNumber(20), "complex_threshold" -> JsNumber(200)),
      )
    }

    "build/configure metrics threshold with given warning and critical levels" in {
      val query = "some_function(over.some.query.for.anything.like*)"
      val serviceConfig = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withMetricsThreshold(MetricsThreshold(name = "alert1", query = query, warning = Some(65), critical = Some(88)))
        .withMetricsThreshold(MetricsThreshold(name = "alert1-warning-only", query = query, warning = Some(44)))
        .withMetricsThreshold(MetricsThreshold(name = "alert1-critical-only", query = query, critical = Some(45)))
        .withMetricsThreshold(MetricsThreshold(name = "alert2", query = query, warning = Some(30.03), critical = Some(12.21), invert = true))
        .build.get.parseJson.asJsObject.fields

      serviceConfig("metricsThresholds") shouldBe JsArray(
        JsObject("name" -> JsString("alert1"), "query" -> JsString(query), "warning" -> JsNumber(65.0), "critical" -> JsNumber(88.0), "invert" ->JsBoolean(false)),
        JsObject("name" -> JsString("alert1-warning-only"), "query" -> JsString(query), "warning" -> JsNumber(44.0), "invert" ->JsBoolean(false)),
        JsObject("name" -> JsString("alert1-critical-only"), "query" -> JsString(query), "critical" -> JsNumber(45.0), "invert" ->JsBoolean(false)),
        JsObject("name" -> JsString("alert2"), "query" -> JsString(query), "warning" -> JsNumber(30.03), "critical" -> JsNumber(12.21), "invert" ->JsBoolean(true))
      )
    }

  }

  "build/configure HttpAbsolutePercentSplitThreshold with required parameters" in {
    val percent = 10.2
    val crossOver = 20
    val absolute = 30
    val hysteresis = 1.2
    val excludeSpikes = 2
    val filter = "Some error"
    val severity = AlertSeverity.Warning

    val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
      .withHttpAbsolutePercentSplitThreshold(HttpAbsolutePercentSplitThreshold(
        percent, crossOver, absolute, hysteresis, excludeSpikes, filter, severity)).build.get.parseJson.asJsObject.fields

    val expected = JsArray(JsObject("errorFilter" -> JsString(filter),
      "absoluteThreshold" -> JsNumber(absolute),
      "crossOver" -> JsNumber(crossOver),
      "excludeSpikes" -> JsNumber(excludeSpikes),
      "hysteresis" -> JsNumber(hysteresis),
      "percentThreshold" -> JsNumber(percent),
      "severity" -> JsString("warning")))

    serviceConfig("absolute-percentage-split-threshold") shouldBe expected
  }

  "build/configure HttpAbsolutePercentSplitDownstreamServiceThreshold with required parameters" in {
    val percent = 10.2
    val crossOver = 20
    val absolute = 30
    val hysteresis = 1.2
    val excludeSpikes = 2
    val filter = "Some error"
    val target = "service.invalid"
    val severity = AlertSeverity.Warning

    val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
      .withHttpAbsolutePercentSplitDownstreamServiceThreshold(HttpAbsolutePercentSplitDownstreamServiceThreshold(
        percent, crossOver, absolute, hysteresis, excludeSpikes, filter, target, severity)).build.get.parseJson.asJsObject.fields

    val expected = JsArray(JsObject("errorFilter" -> JsString(filter),
      "target" -> JsString(target),
      "absoluteThreshold" -> JsNumber(absolute),
      "crossOver" -> JsNumber(crossOver),
      "excludeSpikes" -> JsNumber(excludeSpikes),
      "hysteresis" -> JsNumber(hysteresis),
      "percentThreshold" -> JsNumber(percent),
      "severity" -> JsString("warning")))

    serviceConfig("absolute-percentage-split-downstream-service-threshold") shouldBe expected
  }

  "build/configure HttpAbsolutePercentSplitDownstreamHodThreshold with required parameters" in {
    val percent = 10.2
    val crossOver = 20
    val absolute = 30
    val hysteresis = 1.2
    val excludeSpikes = 2
    val filter = "Some error"
    val target = "hod-endpoint"
    val severity = AlertSeverity.Warning

    val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
      .withHttpAbsolutePercentSplitDownstreamHodThreshold(HttpAbsolutePercentSplitDownstreamHodThreshold(
        percent, crossOver, absolute, hysteresis, excludeSpikes, filter, target, severity)).build.get.parseJson.asJsObject.fields

    val expected = JsArray(JsObject("errorFilter" -> JsString(filter),
      "target" -> JsString(target),
      "absoluteThreshold" -> JsNumber(absolute),
      "crossOver" -> JsNumber(crossOver),
      "excludeSpikes" -> JsNumber(excludeSpikes),
      "hysteresis" -> JsNumber(hysteresis),
      "percentThreshold" -> JsNumber(percent),
      "severity" -> JsString("warning")))

    serviceConfig("absolute-percentage-split-downstream-hod-threshold") shouldBe expected
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

    val expected = JsObject(
      "severity" -> JsString("critical"),
      "count" -> JsNumber(threshold)
    )

    serviceConfig("exception-threshold") shouldBe expected
  }

  "build/configure ExceptionThreshold with optional parameter severity" in {
    val threshold = 12
    val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
      .withExceptionThreshold(threshold, AlertSeverity.Warning).build.get.parseJson.asJsObject.fields

    val expected = JsObject(
      "severity" -> JsString("warning"),
      "count" -> JsNumber(threshold)
    )

    serviceConfig("exception-threshold") shouldBe expected
  }

  "build/configure ErrorsLoggedThreshold with required parameters" in {
    val threshold = 12
    val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
      .withErrorsLoggedThreshold(threshold).build.get.parseJson.asJsObject.fields

    serviceConfig("errors-logged-threshold") shouldBe JsNumber(threshold)
  }

  "build/configure http5xxPercentThreshold with required parameters" in {
    val threshold = 13.3
    val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
      .withHttp5xxPercentThreshold(threshold).build.get.parseJson.asJsObject.fields

    serviceConfig("5xx-percent-threshold") shouldBe JsObject(
      "severity" -> JsString("critical"),
      "percentage" -> JsNumber(threshold)
    )
  }

  "build/configure averageCPUThreshold with required parameters" in {
    val threshold = 15
    val serviceConfig: Map[String, JsValue] = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
      .withAverageCPUThreshold(threshold).build.get.parseJson.asJsObject.fields

    serviceConfig("average-cpu-threshold") shouldBe JsNumber(threshold)
  }
}
