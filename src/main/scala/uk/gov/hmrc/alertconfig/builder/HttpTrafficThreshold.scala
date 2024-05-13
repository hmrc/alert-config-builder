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

/**
 * This alert will notify you when the total number of requests received by the microservice is below a certain threshold.
 *
 * One or both of warning and critical must be given.
 *
 * @param warning The number of http requests below which a warning level alert will be raised
 * @param critical The number of http requests below which a critical level alert will be raised
 * @param maxMinutesBelowThreshold The number of minutes over which the threshold breaching triggers an alert
 * @param alertingPlatform The platform this alert will target. We are migrating towards Grafana and away from Sensu
 */
case class HttpTrafficThreshold(
    warning: Option[Int],
    critical: Option[Int],
    maxMinutesBelowThreshold: Int = 5,
    alertingPlatform: AlertingPlatform = AlertingPlatform.Default
)

object HttpTrafficThresholdProtocol {

  import DefaultJsonProtocol._

  implicit val thresholdFloorFormat: JsonFormat[HttpTrafficThreshold] = {
    jsonFormat4(HttpTrafficThreshold)
  }

}
