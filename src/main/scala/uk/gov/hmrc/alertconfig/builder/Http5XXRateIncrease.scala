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

case class Http5xxRateIncrease(
    name: String,
    simple_threshold: Int,
    complex_threshold: Int
)

object Http5xxRateIncreaseProtocol extends DefaultJsonProtocol {
  implicit val rateIncreaseFormat: JsonFormat[Http5xxRateIncrease] = jsonFormat3(Http5xxRateIncrease)
}
