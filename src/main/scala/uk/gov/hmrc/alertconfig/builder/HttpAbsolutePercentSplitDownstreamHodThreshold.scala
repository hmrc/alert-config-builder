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

case class HttpAbsolutePercentSplitDownstreamHodThreshold(
    percentThreshold: Double = 100.0,
    crossOver: Int = 0,
    absoluteThreshold: Int = Int.MaxValue,
    hysteresis: Double = 1.0,
    excludeSpikes: Int = 0,
    errorFilter: String = "status:>498",
    target: String = "",
    severity: AlertSeverity = AlertSeverity.Critical
)
