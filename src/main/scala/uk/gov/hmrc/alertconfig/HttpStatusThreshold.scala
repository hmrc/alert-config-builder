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

object HttpStatus {
  sealed trait HttpStatusType {
    val status: Int
  }
  case class HTTP_STATUS(val status: Int) extends HttpStatusType
  case object HTTP_STATUS_429 extends HTTP_STATUS(429)
  case object HTTP_STATUS_499 extends HTTP_STATUS(499)
  case object HTTP_STATUS_500 extends HTTP_STATUS(500)
  case object HTTP_STATUS_501 extends HTTP_STATUS(501)
  case object HTTP_STATUS_502 extends HTTP_STATUS(502)
  case object HTTP_STATUS_503 extends HTTP_STATUS(503)
  case object HTTP_STATUS_504 extends HTTP_STATUS(504)
}

object HttpStatusThresholdProtocol extends DefaultJsonProtocol {

  implicit val httpStatusFormat = jsonHttpStatusEnum(HttpStatus)

  implicit val severityFormat = jsonSeverityEnum(AlertSeverity)

  implicit val thresholdFormat = jsonFormat3(HttpStatusThreshold)
}
