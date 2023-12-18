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

case class HttpStatusPercentThreshold(
    httpStatus: HttpStatus.HTTP_STATUS,
    percentage: Double = 100.0,
    severity: AlertSeverity = AlertSeverity.Critical,
    httpMethod: HttpMethod = HttpMethod.All
)

object HttpStatusPercentThresholdProtocol {
  import DefaultJsonProtocol._

  implicit val thresholdPercentFormat: JsonFormat[HttpStatusPercentThreshold] = {
    implicit val hsf: JsonFormat[HttpStatus.HTTP_STATUS] = httpStatusFormat
    implicit val asf: JsonFormat[AlertSeverity]          = alertSeverityFormat
    implicit val hmf: JsonFormat[HttpMethod]             = httpMethodFormat
    jsonFormat4(HttpStatusPercentThreshold)
  }

}
