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

import spray.json.{DefaultJsonProtocol, JsonFormat, RootJsonFormat}
import uk.gov.hmrc.alertconfig.AlertSeverity.AlertSeverityType

case class Http5xxPercentThreshold(
  percentage: Double            = 100.0,
  severity  : AlertSeverityType = AlertSeverity.critical
)

object Http5xxPercentThresholdProtocol extends DefaultJsonProtocol {
  implicit val severityFormat = jsonSeverityEnum(AlertSeverity)
  implicit val thresholdFormat = jsonFormat2(Http5xxPercentThreshold)
}
