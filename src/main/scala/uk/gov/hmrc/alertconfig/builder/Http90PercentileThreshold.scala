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

/** This alert will notify you when the 90th Percentile request time goes above the defined thresholds for the time period specified by the user.
  *
  * One or both of warning and critical must be given.
  *
  * @param warning
  *   The response time in millisecond above which a warning level alert will be raised
  * @param critical
  *   The response time in millisecond above which a critical level alert will be raised
  * @param timePeriod
  *   How far back to consider in minutes. Default is 15 minutes. Range: 1 - 15 (inclusive)
  */
case class Http90PercentileResponseTimeThreshold(
    warning: Option[Int],
    critical: Option[Int],
    timePeriod: Int = 15
)
