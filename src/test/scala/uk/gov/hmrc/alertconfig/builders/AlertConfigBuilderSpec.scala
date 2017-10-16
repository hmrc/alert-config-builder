/*
 * Copyright 2017 HM Revenue & Customs
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
import uk.gov.hmrc.alertconfig.HttpStatusThreshold

class AlertConfigBuilderSpec extends WordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach() {
    System.setProperty("app-config-path", "src/test/resources/app-config")
    System.setProperty("zone-mapping-path", "src/test/resources/zone-to-service-domain-mapping.yml")
  }

  "AlertConfigBuilder" should {
    "build correct config" in  {

      val config = AlertConfigBuilder("service1", handlers = Seq("h1","h2"))
        .withContainerKillThreshold(56).build.get.parseJson.asJsObject.fields

      config("app") shouldBe JsString("service1.domain.zone.1")
      config("handlers") shouldBe JsArray(JsString("h1"), JsString("h2"))
      config("exception-threshold") shouldBe JsNumber(2)
      config("5xx-threshold") shouldBe JsNumber(Int.MaxValue)
      config("5xx-percent-threshold") shouldBe JsNumber(100)
      config("containerKillThreshold") shouldBe JsNumber(56)

    }

    "throw exception and stop processing when app config directory not found" in {
      System.setProperty("app-config-path", "this-directory-does-not-exist")

      intercept[FileNotFoundException] {
        val config = AlertConfigBuilder("service1", handlers = Seq("h1","h2")).build.get.parseJson.asJsObject.fields
      }
    }

    "Returns None when app config file not found" in {
        AlertConfigBuilder("absent-service", handlers = Seq("h1","h2")).build shouldBe None
    }

    "Returns None when app config file exists but zone key is absent" in {
      AlertConfigBuilder("service-with-absent-zone-key", handlers = Seq("h1","h2")).build shouldBe None
    }

    "Returns None when app config file exists but it unparsable" in {
      AlertConfigBuilder("service-with-unparseable-app-config", handlers = Seq("h1","h2")).build shouldBe None
    }

    "Maps the correct service domain" in {
      val service2Config = AlertConfigBuilder("service2", handlers = Seq("h1","h2")).build.get.parseJson.asJsObject.fields
      val service3Config = AlertConfigBuilder("service3", handlers = Seq("h1","h2")).build.get.parseJson.asJsObject.fields
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

    "build/configure http status threshold with given thresholds" in {

      val serviceConfig = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_502, 2))
        .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_503, 3))
        .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_504, 4)).build.get.parseJson.asJsObject.fields

      serviceConfig("httpStatusThresholds") shouldBe JsArray(
        JsObject("httpStatus" -> JsNumber(502),"count" ->  JsNumber(2)),
        JsObject("httpStatus" -> JsNumber(503),"count" ->  JsNumber(3)),
        JsObject("httpStatus" -> JsNumber(504),"count" ->  JsNumber(4))
      )
    }

    "build/configure logMessageThresholds with given thresholds" in {

      val serviceConfig = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
          .withLogMessageThreshold("SIMUATED_ERROR1" , 3)
          .withLogMessageThreshold("SIMUATED_ERROR2" , 4)
          .withLogMessageThreshold("SIMUATED_ERROR3" , 5).build.get.parseJson.asJsObject.fields

      serviceConfig("log-message-thresholds") shouldBe JsArray(
        JsObject("message" -> JsString("SIMUATED_ERROR1"),"count" ->  JsNumber(3)),
        JsObject("message" -> JsString("SIMUATED_ERROR2"),"count" ->  JsNumber(4)),
        JsObject("message" -> JsString("SIMUATED_ERROR3"),"count" ->  JsNumber(5))
      )
    }

    "build/configure kibanaQueryThresholds with given thresholds" in {

      val serviceConfig = AlertConfigBuilder("service1", handlers = Seq("h1", "h2"))
        .withKibanaQueryThreshold("SIMULATED_ERROR1", "message: QUERY1", 3)
        .withKibanaQueryThreshold("SIMULATED_ERROR2", "message: QUERY2", 4)
        .withKibanaQueryThreshold("SIMULATED_ERROR3", "message: \"QUERY3\"" , 5).build.get.parseJson.asJsObject.fields

      serviceConfig("kibana-query-thresholds") shouldBe JsArray(
        JsObject("name" -> JsString("SIMULATED_ERROR1"), "query" -> JsString("message: QUERY1"), "count" ->  JsNumber(3)),
        JsObject("name" -> JsString("SIMULATED_ERROR2"), "query" -> JsString("message: QUERY2"), "count" ->  JsNumber(4)),
        JsObject("name" -> JsString("SIMULATED_ERROR3"), "query" -> JsString("message: \"QUERY3\""), "count" ->  JsNumber(5))
      )
    }

    "build/configure any empty http status threshold" in {

      val serviceConfig = AlertConfigBuilder("service1").build.get.parseJson.asJsObject.fields

      serviceConfig("httpStatusThresholds") shouldBe JsArray()
    }
  }
}
