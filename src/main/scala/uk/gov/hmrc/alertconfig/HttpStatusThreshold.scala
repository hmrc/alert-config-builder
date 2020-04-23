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

package uk.gov.hmrc.alertconfig

import spray.json.DefaultJsonProtocol
import uk.gov.hmrc.alertconfig.HttpStatus.HttpStatusType
import uk.gov.hmrc.alertconfig.AlertSeverity.AlertSeverityType


case class HttpStatusThreshold(httpStatus: HttpStatusType, count: Int = 1, severity: AlertSeverityType = AlertSeverity.critical)


object HttpStatus extends Enumeration {

  type HttpStatusType = Value
  val HTTP_STATUS_429 = Value(429)
  val HTTP_STATUS_499 = Value(499)
  val HTTP_STATUS_500 = Value(500)
  val HTTP_STATUS_501 = Value(501)
  val HTTP_STATUS_502 = Value(502)
  val HTTP_STATUS_503 = Value(503)
  val HTTP_STATUS_504 = Value(504)
}

object HttpStatusThresholdProtocol extends DefaultJsonProtocol {

  implicit val httpStatusFormat = jsonHttpStatusEnum(HttpStatus)

  implicit val severityFormat = jsonSeverityEnum(AlertSeverity)

  implicit val thresholdFormat = jsonFormat3(HttpStatusThreshold)
}
