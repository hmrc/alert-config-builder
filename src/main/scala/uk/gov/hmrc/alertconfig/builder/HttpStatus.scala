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

object HttpStatus {
  case class HTTP_STATUS(status: Int)
  val HTTP_STATUS_429: HTTP_STATUS = HTTP_STATUS(429)
  val HTTP_STATUS_499: HTTP_STATUS = HTTP_STATUS(499)
  val HTTP_STATUS_500: HTTP_STATUS = HTTP_STATUS(500)
  val HTTP_STATUS_501: HTTP_STATUS = HTTP_STATUS(501)
  val HTTP_STATUS_502: HTTP_STATUS = HTTP_STATUS(502)
  val HTTP_STATUS_503: HTTP_STATUS = HTTP_STATUS(503)
  val HTTP_STATUS_504: HTTP_STATUS = HTTP_STATUS(504)
}
