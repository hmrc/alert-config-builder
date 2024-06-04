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

import uk.gov.hmrc.alertconfig.builder.custom.CloudWatchSource.CloudWatchSource
import uk.gov.hmrc.alertconfig.builder.custom.CustomAlertSeverity.AlertSeverity
import uk.gov.hmrc.alertconfig.builder.custom.EvaluationOperator.EvaluationOperator
import uk.gov.hmrc.alertconfig.builder.custom.ReducerFunction.ReducerFunction
import uk.gov.hmrc.alertconfig.builder.custom.Statistic.Statistic

/** CloudWatch metrics based alert.
  *
  * @param alertName
  *   Name that the alert will be created with
  * @param checkIntervalMinutes
  *   Number of minutes between each check
  * @param dashboardPanelId
  *   Specific panel to deep link to that is specific to this alert
  * @param dashboardUri
  *   Grafana uri to link to. This should just be the uri path and not include the domain
  * @param integrations
  *   Which PagerDuty integrations to direct this alert to n
  * @param luceneQuery
  *   Query to make to Elasticsearch
  * @param operator
  *   Whether to evaluate the metric as greater than or less than
  * @param reducerFunction
  *   Function to use when manipulate data returned from query
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
  */
case class CustomElasticsearchAlert(
    alertName: String,
    checkIntervalMinutes: Int,
    dashboardPanelId: Option[Int],
    dashboardUri: Option[String],
    integrations: Seq[String],
    luceneQuery: String,
    operator: EvaluationOperator,
    reducerFunction: Option[ReducerFunction] = Some(ReducerFunction.LAST),
    runbookUrl: Option[String],
    severity: AlertSeverity,
    summary: String,
    teamName: String,
    thresholds: EnvironmentThresholds,
) extends CustomAlert