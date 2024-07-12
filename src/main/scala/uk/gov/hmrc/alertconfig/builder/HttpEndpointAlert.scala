/*
 * Copyright 2024 HM Revenue & Customs
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

/** This alert will notify when it fails to receive the expected response (status code and/or string pattern) from the given endpoint
  *
  * @param httpEndpoint
  *   The HTTP endpoint to be checked.
  * @param cronCheckSchedule
  *   The schedule for the cron job that will check the endpoint.
  * @param expectedHttpStatusCode
  *   The expected HTTP status code from the endpoint. Defaults to 200.
  * @param expectedQueryString
  *   The expected query string in the endpoint's response.
  * @param severity
  *   The severity level of the alert (critical or warning). Defaults to critical.
  * @param alertingPlatform
  *   The platform this alert will target. Defaults to the platform being migrated towards (e.g., Grafana).
  */
case class HttpEndpointAlert(
    httpEndpoint: String,
    cronCheckSchedule: String,
    expectedHttpStatusCode: Int = 200,
    expectedQueryString: String = "",
    severity: AlertSeverity = AlertSeverity.Critical,
    alertingPlatform: AlertingPlatform = AlertingPlatform.Default
)

object HttpEndpointAlertProtocol extends DefaultJsonProtocol {
  import DefaultJsonProtocol._

  implicit val thresholdFormat: JsonFormat[HttpEndpointAlert] = {
    implicit val asf: JsonFormat[AlertSeverity] = alertSeverityFormat
    jsonFormat6(HttpEndpointAlert)
  }

}
