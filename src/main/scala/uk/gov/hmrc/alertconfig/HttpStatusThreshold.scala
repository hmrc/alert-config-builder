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
import spray.json.{DefaultJsonProtocol, JsNumber, JsValue, JsonFormat}
import uk.gov.hmrc.alertconfig.AlertSeverity.AlertSeverityType
import uk.gov.hmrc.alertconfig.HttpMethod.HttpMethodType
import uk.gov.hmrc.alertconfig.HttpStatus.HTTP_STATUS

case class HttpStatusThreshold(httpStatus: HTTP_STATUS, count: Int = 1, severity: AlertSeverityType = AlertSeverity.critical, httpMethod: HttpMethodType = HttpMethod.all)

object HttpStatusThresholdProtocol extends DefaultJsonProtocol {

  implicit object httpStatusFormat extends JsonFormat[HTTP_STATUS] {
    override def read(json: JsValue): HTTP_STATUS = HTTP_STATUS(IntJsonFormat.read(json))
    override def write(obj: HTTP_STATUS): JsValue = JsNumber(obj.status)
  }

  implicit val severityFormat = jsonSeverityEnum(AlertSeverity)
  implicit val methodFormat = jsonHttpMethodEnum(HttpMethod)
  implicit val thresholdFormat = jsonFormat4(HttpStatusThreshold)
}
