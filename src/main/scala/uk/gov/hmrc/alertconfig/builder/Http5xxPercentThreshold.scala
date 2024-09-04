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

/** This alert will notify when the percentage of http responses returning a 5xx http status code exceeds a given threshold within a 15-minute window.
  *
  * This alert is enabled by default at 100%, but can be disabled by setting it to >100.
  *
  * @param percentage
  *   The percentage of all http responses with a 5xx status code to alert on
  * @param minimumHttp5xxCountThreshold
  *   The minimum count of 5xxs that must be present for the percentThreshold check to kick in. Useful if, for example, you don't want to alert on
  *   just a one-off 5xx in the middle of the night.
  * @param severity
  *   Whether to raise the alert as critical or warning
  */
case class Http5xxPercentThreshold(
    percentage: Double,
    minimumHttp5xxCountThreshold: Int = 0,
    severity: AlertSeverity = AlertSeverity.Critical
)
