/*
 * Copyright 2021 HM Revenue & Customs
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

// By default we alert if the count of messages is >= threshold. If lessThanMode is set we alert if < threshold
case class LogMessageThreshold(message: String, count: Int, lessThanMode: Boolean = false)

object LogMessageThresholdProtocol extends DefaultJsonProtocol {

  implicit val format = jsonFormat3(LogMessageThreshold)
}
