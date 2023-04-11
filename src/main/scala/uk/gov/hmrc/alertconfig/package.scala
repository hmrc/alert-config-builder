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

package uk.gov.hmrc.alertconfig

import spray.json.{DeserializationException, JsNumber, JsString, JsValue, JsonFormat}

package object builder {

  def jsonSeverityEnum(enu: AlertSeverity.type) = new JsonFormat[AlertSeverity.Value] {
    def write(obj: AlertSeverity.AlertSeverityType) = JsString(obj.toString)

    def read(json: JsValue) = json match {
      case JsNumber(num) => AlertSeverity(num.toInt)
      case something => throw DeserializationException(s"Expected a value from enum $enu instead of $something")
    }
  }

  def jsonHttpMethodEnum(enu: HttpMethod.type) = new JsonFormat[HttpMethod.Value] {
    def write(obj: HttpMethod.HttpMethodType) = JsString(obj.toString)

    def read(json: JsValue) = json match {
      case JsNumber(num) => HttpMethod(num.toInt)
      case something => throw DeserializationException(s"Expected a value from enum $enu instead of $something")
    }
  }
}
