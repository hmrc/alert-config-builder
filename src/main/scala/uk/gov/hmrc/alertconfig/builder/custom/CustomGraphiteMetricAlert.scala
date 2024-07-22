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
import uk.gov.hmrc.alertconfig.builder.custom.EvaluationOperator.EvaluationOperator
import uk.gov.hmrc.alertconfig.builder.custom.ReducerFunction.ReducerFunction
import uk.gov.hmrc.alertconfig.builder.custom.TimeRangeAsMinutes.TimeRangeAsMinutes

/** Graphite metric based alert.
  *
  * @param alertName
  *   Name that the alert will be created with
  * @param pendingPeriodMinutes
  *   Amount of time in minutes that a threshold needs to be breached before the alert fires
  * @param dashboardUri
  *   Grafana uri to link to. This should just be the uri path and not include the domain
  * @param dashboardPanelId
  *   Specific panel to deep link to that is specific to this alert
  * @param integrations
  *   Which PagerDuty integrations to direct this alert to
  * @param operator
  *   Whether to evaluate the metric as greater than or less than
  * @param query
  *   Graphite query you're running
  * @param teamName
  *   All alerts are prefixed with the team name
  * @param reducerFunction
  *   Function to use to transform multiple data points returned from query into a single value, to be compared against the specified threshold. Valid values include: COUNT, LAST, MAX, MEAN, MIN, SUM.
  *   Note: Using the LAST reducer could result in not all data points being considered during alert evaluation, depending on the frequency at which the alert runs.
  *   Example: An alert with a LAST reducer, that runs every 2 minutes, based on a metric that is written on a per minute basis, will only consider ~50% of the data points, potentially missing a legitimate breach of an alert threshold.
  * @param runbookUrl
  *   Runbook for when this alert fires
  * @param severity
  *   The severity of this alert. E.g. Warning or Critical
  * @param summary
  *   The description to populate in PagerDuty when the alert fires
  * @param thresholds
  *   Trigger point for each environment
 * @param queryTimeRangeMinutes The sample period to check data for. If you set it to FIVE_MINUTES, the alert check will evaluate data starting from 6 minutes ago until one minute ago (so that only fully shipped metrics are evaluated).
  */
case class CustomGraphiteMetricAlert(
    alertName: String,
    pendingPeriodMinutes: Option[Int] = None,
    dashboardUri: Option[String],
    dashboardPanelId: Option[Int],
    integrations: Seq[String],
    operator: EvaluationOperator,
    query: String,
    teamName: String,
    reducerFunction: ReducerFunction,
    runbookUrl: Option[String],
    severity: AlertSeverity,
    summary: String,
    thresholds: EnvironmentThresholds,
    queryTimeRangeMinutes: TimeRangeAsMinutes = TimeRangeAsMinutes.FIFTEEN_MINUTES
) extends CustomAlert
