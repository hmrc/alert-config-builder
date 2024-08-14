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
import uk.gov.hmrc.alertconfig.builder.custom.CheckIntervalMinutes.CheckIntervalMinutes
import uk.gov.hmrc.alertconfig.builder.custom.TimeRangeAsMinutes.TimeRangeAsMinutes

/** This alert will notify when it fails to receive the expected response (status code and/or string pattern) from the given endpoint
  * @param checkIntervalMinutes
  *   Number of minutes between each check. See [[CheckIntervalMinutes]] for supported values
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
  * @param pendingPeriodMinutes
  *   Amount of time in minutes that a threshold needs to be breached before the alert fires
  * @param queryTimeRangeMinutes
  *   The sample period to check data for. If you set it to FIVE_MINUTES, the alert check will evaluate data starting from 6 minutes ago until one
  *   minute ago (so that only fully shipped metrics are evaluated).
  * @param runbookUrl
  *   Runbook for when this alert fires
  * @param severity
  *   The severity level of the alert (critical or warning). Defaults to critical.
  * @param summary
  *   The description to populate in PagerDuty when the alert fires
  * @param teamName
  *   All alerts are prefixed with the team name
  */
@JsonFilter("RemoveEnvironmentsEnabledField")
case class CustomHttpEndpointAlert(
    checkIntervalMinutes: Option[CheckIntervalMinutes] = None,
    checkName: String,
    componentName: String,
    cronCheckSchedule: String,
    environmentsEnabled: EnvironmentsEnabled,
    expectedHttpStatusCode: Option[Int] = None,
    expectedStringInResponse: Option[String] = None,
    httpEndpoint: String,
    integrations: Seq[String],
    pendingPeriodMinutes: Option[Int] = None,
    queryTimeRangeMinutes: TimeRangeAsMinutes = TimeRangeAsMinutes.ONE_HOUR,
    runbookUrl: Option[String],
    severity: AlertSeverity,
    summary: String,
    teamName: String
) extends CustomAlert
