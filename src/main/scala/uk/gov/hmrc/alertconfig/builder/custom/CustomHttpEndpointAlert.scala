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

package uk.gov.hmrc.alertconfig.builder.custom

import uk.gov.hmrc.alertconfig.builder.custom.CustomAlertSeverity.AlertSeverity
import com.fasterxml.jackson.annotation.JsonFilter

/** This alert will notify when it fails to receive the expected response (status code and/or string pattern) from the given endpoint
  *
  * @param checkName
  *   Name to be used in the metric path. Only lowercase letters and - are allowed: `^[a-z-]+$`
  * @param componentName
  *   The name of the component you are testing. e.g. jira / proxy / sensu / mongo. Only lowercase letters and - are allowed: `^[a-z-]+$`
  * @param cronCheckSchedule
  *   The cron schedule for how often the endpoint will be checked creating a metric data point.
  * @param environmentsEnabled
  *   The specific environments to enable this alert in
  * @param expectedHttpStatusCode
  *   The HTTP status code expected from the endpoint.
  * @param expectedStringInResponse
  *   A substring that is expect in the endpoint's response.
  * @param httpEndpoint
  *   The HTTP endpoint to be checked.
  * @param integrations
  *   Which PagerDuty integrations to direct this alert to
  * @param runbookUrl
  *   Runbook for when this alert fires
  * @param severity
  *   The severity level of the alert (critical or warning). Defaults to critical.
  */
@JsonFilter("RemoveEnvironmentsEnabledField")
case class CustomHttpEndpointAlert(
    checkName: String,
    componentName: String,
    cronCheckSchedule: String,
    environmentsEnabled: EnvironmentsEnabled,
    expectedHttpStatusCode: Option[Int] = None,
    expectedStringInResponse: Option[String] = None,
    httpEndpoint: String,
    integrations: Seq[String],
    runbookUrl: Option[String],
    severity: AlertSeverity
) extends CustomAlert
