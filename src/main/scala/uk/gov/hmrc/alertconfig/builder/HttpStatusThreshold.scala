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

import spray.json.{DefaultJsonProtocol, JsonFormat}

/** This alert will notify when your microservice returns a specified number of requests with a given http status code (between 400 and 599) within a
  * 15-minute window.
  * @param httpStatus
  *   The http status code that this alert will trigger on (429, 499-504)
  * @param count
  *   The number of http responses with the given status code to alert on
  * @param severity
  *   Whether to raise the alert as critical or warning
  * @param httpMethod
  *   The http method to filter all requests by (one of All, Post, Get, Put, Delete)
  * @param alertingPlatform
  *   The platform this alert will target. We are migrating towards Grafana and away from Sensu
  */
case class HttpStatusThreshold(
    httpStatus: HttpStatus.HTTP_STATUS,
    count: Int = 1,
    severity: AlertSeverity = AlertSeverity.Critical,
    httpMethod: HttpMethod = HttpMethod.All,
    alertingPlatform: AlertingPlatform = AlertingPlatform.Default
)

object HttpStatusThresholdProtocol {
  import DefaultJsonProtocol._

  implicit val thresholdFormat: JsonFormat[HttpStatusThreshold] = {
    implicit val hsf: JsonFormat[HttpStatus.HTTP_STATUS] = httpStatusFormat
    implicit val asf: JsonFormat[AlertSeverity]          = alertSeverityFormat
    implicit val hmf: JsonFormat[HttpMethod]             = httpMethodFormat
    jsonFormat5(HttpStatusThreshold)
  }

}
