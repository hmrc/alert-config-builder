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
import uk.gov.hmrc.alertconfig.builder.custom.TimeRangeAsMinutes.TimeRangeAsMinutes

/** Custom ElasticSearch alert based on the percentage of a subset of records.
  *
  * @param alertName
  *   Name that the alert will be created with
  * @param checkIntervalMinutes
  *   Number of minutes between each check. See [[CheckIntervalMinutes]] for supported values
  * @param integrations
  *   Which PagerDuty integrations to direct this alert to n
  * @param kibanaDashboardUri
  *   (Optional) Kibana uri to link to. This should just be the uri path and not include the domain
  * @param luceneQuerySubset
  *   Query to make to Elasticsearch to get subset of records (to calculate as percentage of luceneQueryTotal)
  * @param luceneQueryTotal
  *   Query to make to Elasticsearch to get total records (to calculate percentage from)
  * @param operator
  *   Whether to evaluate the metric as greater than or less than
  * @param pendingPeriodMinutes
  *   Amount of time in minutes that a threshold needs to be breached before the alert fires. Defaults to fire immediately
  * @param queryTimeRangeMinutes
  *   The sample period to check data for. If you set it to FIVE_MINUTES, the alert check will evaluate data starting from 6 minutes ago until one
  *   minute ago (so that only fully shipped metrics are evaluated).
  * @param runbookUrl
  *   (Optional) Runbook for when this alert fires
  * @param severity
  *   The severity of this alert. E.g. Warning or Critical
  * @param summary
  *   The description to populate in PagerDuty when the alert fires
  * @param teamName
  *   All alerts are prefixed with the team name
  * @param thresholds
  *   Trigger point for each environment
  */
case class CustomElasticsearchPercentageAlert(
    alertName: String,
    checkIntervalMinutes: Option[CheckIntervalMinutes] = None,
    integrations: Seq[String],
    kibanaDashboardUri: Option[String] = None,
    luceneQuerySubset: String,
    luceneQueryTotal: String,
    operator: EvaluationOperator = EvaluationOperator.GREATER_THAN,
    pendingPeriodMinutes: Option[Int] = None,
    queryTimeRangeMinutes: TimeRangeAsMinutes = TimeRangeAsMinutes.FIFTEEN_MINUTES,
    runbookUrl: Option[String] = None,
    severity: AlertSeverity,
    summary: String,
    teamName: String,
    thresholds: EnvironmentThresholds
) extends CustomAlert
