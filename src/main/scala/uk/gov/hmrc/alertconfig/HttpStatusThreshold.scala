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

package uk.gov.hmrc.alertconfig

import spray.json.DefaultJsonProtocol
import uk.gov.hmrc.alertconfig.HttpStatus.HttpStatusType


case class HttpStatusThreshold(httpStatus: HttpStatusType, count: Int = 1)


object HttpStatus extends Enumeration {

  type HttpStatusType = Value
  val HTTP_STATUS_400 = Value(400)
  val HTTP_STATUS_401 = Value(401)
  val HTTP_STATUS_403 = Value(403)
  val HTTP_STATUS_404 = Value(404)
  val HTTP_STATUS_405 = Value(405)
  val HTTP_STATUS_409 = Value(409)
  val HTTP_STATUS_415 = Value(415)
  val HTTP_STATUS_500 = Value(500)
  val HTTP_STATUS_501 = Value(501)
  val HTTP_STATUS_502 = Value(502)
  val HTTP_STATUS_503 = Value(503)
  val HTTP_STATUS_504 = Value(504)
}


object HttpStatusThresholdProtocol extends DefaultJsonProtocol {

  implicit val httpStatusFormat = jsonHttpStatusEnum(HttpStatus)

  implicit val thresholdFormat = jsonFormat2(HttpStatusThreshold)
}
