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
 * This alert will notify when the count of a given log message is logged exceeds a given threshold within a 15-minute window.
 *
 * By default we alert if the count of messages is >= threshold. If lessThanMode is set we alert if < threshold
 *
 * @param message The substring to search for in the log message
 * @param count The threshold above which an alert will be raised
 * @param lessThanMode If true, flips the logic so that an alert is raised if less than the threshold amount is detected
 * @param severity The severity to set for this check in PagerDuty
 * @param alertingPlatform The platform this alert will target. We are migrating towards Grafana and away from Sensu
 */
case class LogMessageThreshold(
    message: String,
    count: Int,
    lessThanMode: Boolean = false,
    severity: AlertSeverity = AlertSeverity.Critical,
    alertingPlatform: AlertingPlatform = AlertingPlatform.Default
)

object LogMessageThresholdProtocol extends {
  import DefaultJsonProtocol._

  implicit val logMessageThresholdFormat: JsonFormat[LogMessageThreshold] = {
    implicit val asf: JsonFormat[AlertSeverity] = alertSeverityFormat
    jsonFormat5(LogMessageThreshold)
  }

}
