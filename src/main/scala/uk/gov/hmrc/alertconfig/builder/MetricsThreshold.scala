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

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class MetricsThreshold(
    name: String,
    query: String,
    warning: Option[Double] = None,
    critical: Option[Double] = None,
    invert: Boolean = false,
    alertingPlatform: AlertingPlatform = AlertingPlatform.Default
    ,
    reducer: String = "mean", // TODO finite set including mean, last

    // todo object for overrides
    dashboardOverride: String = "",
    dashboardPanelOverride: String = "",
    runbookUrl: String = "",
    summary: String = ""
)

object MetricsThresholdProtocol extends DefaultJsonProtocol {
  implicit val thresholdFormat: RootJsonFormat[MetricsThreshold] = jsonFormat11(MetricsThreshold)
}
