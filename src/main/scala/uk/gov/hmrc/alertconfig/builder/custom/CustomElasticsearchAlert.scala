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

import uk.gov.hmrc.alertconfig.builder.custom.CheckIntervalMinutes.CheckIntervalMinutes
import uk.gov.hmrc.alertconfig.builder.custom.CustomAlertSeverity.AlertSeverity
import uk.gov.hmrc.alertconfig.builder.custom.EvaluationOperator.EvaluationOperator
import uk.gov.hmrc.alertconfig.builder.custom.ReducerFunction.ReducerFunction
import uk.gov.hmrc.alertconfig.builder.custom.TimeRangeAsMinutes.TimeRangeAsMinutes

/** CloudWatch metrics based alert.
  *
  * @param alertName
  *   Name that the alert will be created with
  * @param checkIntervalMinutes
  *   Number of minutes between each check. See [[CheckIntervalMinutes]] for supported values
  * @param kibanaDashboardUri
  *   Kibana uri to link to. This should just be the uri path and not include the domain
  * @param integrations
  *   Which PagerDuty integrations to direct this alert to n
  * @param luceneQuery
  *   Query to make to Elasticsearch
  * @param operator
  *   Whether to evaluate the metric as greater than or less than
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
  * @param teamName
  *   All alerts are prefixed with the team name
  * @param thresholds
  *   Trigger point for each environment
  * @param evaluationPeriodStartMinutesAgo The start of the evaluation period. If you set to FIFTEEN_MINUTES, the alert check will evaluate data starting fifteen minutes ago up until evaluationPeriodEndMinutesAgo
  * @param evaluationPeriodEndMinutesAgo The end of the evaluation period. If you set it to ONE MINUTE, the alert check will evaluate data starting from evaluationPeriodStartMinutesAgo until one minute ago. Less than one minute ago is not advised because these metrics may not yet be fully shipped.
  */
case class CustomElasticsearchAlert(
    alertName: String,
    checkIntervalMinutes: CheckIntervalMinutes,
    kibanaDashboardUri: Option[String],
    integrations: Seq[String],
    luceneQuery: String,
    operator: EvaluationOperator,
    reducerFunction: ReducerFunction,
    runbookUrl: Option[String],
    severity: AlertSeverity,
    summary: String,
    teamName: String,
    thresholds: EnvironmentThresholds,
    evaluationPeriodStartMinutesAgo: TimeRangeAsMinutes,
    evaluationPeriodEndMinutesAgo: TimeRangeAsMinutes = TimeRangeAsMinutes.ONE_MINUTE
) extends CustomAlert
