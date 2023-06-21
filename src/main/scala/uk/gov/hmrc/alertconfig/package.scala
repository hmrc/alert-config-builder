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

import spray.json.{DefaultJsonProtocol, JsNumber, JsString, JsValue, JsonFormat, deserializationError}

package object builder {

  val alertSeverityFormat = new JsonFormat[AlertSeverity] {
    override def write(obj: AlertSeverity): JsValue =
      JsString(obj.toString)

    override def read(json: JsValue): AlertSeverity =
      Seq(AlertSeverity.Warning, AlertSeverity.Critical)
        .find(_.toString == json.toString)
        .getOrElse(deserializationError("Invalid AlertSeverity"))
  }

  val httpMethodFormat = new JsonFormat[HttpMethod] {
    override def write(obj: HttpMethod): JsValue =
      JsString(obj.toString)

    override def read(json: JsValue): HttpMethod =
      Seq(HttpMethod.All, HttpMethod.Post, HttpMethod.Get, HttpMethod.Put, HttpMethod.Delete)
        .find(_.toString == json.toString)
        .getOrElse(deserializationError("Invalid HttpMethod"))
  }

  val httpStatusFormat = new JsonFormat[HttpStatus.HTTP_STATUS] {
    import DefaultJsonProtocol._
    override def read(json: JsValue): HttpStatus.HTTP_STATUS =
      HttpStatus.HTTP_STATUS(IntJsonFormat.read(json))

    override def write(obj: HttpStatus.HTTP_STATUS): JsValue =
      JsNumber(obj.status)
  }
}
