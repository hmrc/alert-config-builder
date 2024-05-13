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

/**
 * This alert will notify when a given metric query exceeds a given threshold within a 15-minute window.
 *
 * One or both of warning and critical must be given.
 *
 * @param name A unique name to give this alert, which will be used as the name of the alert in PagerDuty
 * @param query The metric path to use to trigger this alert
 * @param warning The response time in millisecond above which a warning level alert will be raised
 * @param critical The response time in millisecond above which a critical level alert will be raised
 * @param invert Set to true to invert the threshold (trigger on below instead of above)
 * @param alertingPlatform The platform this alert will target. We are migrating towards Grafana and away from Sensu
 */
case class MetricsThreshold(
    name: String,
    query: String,
    warning: Option[Double] = None,
    critical: Option[Double] = None,
    invert: Boolean = false,
    alertingPlatform: AlertingPlatform = AlertingPlatform.Default
)

object MetricsThresholdProtocol extends DefaultJsonProtocol {
  implicit val thresholdFormat: RootJsonFormat[MetricsThreshold] = jsonFormat6(MetricsThreshold)
}
