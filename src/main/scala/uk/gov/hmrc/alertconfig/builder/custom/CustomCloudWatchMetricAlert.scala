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

/**
 * CloudWatch metrics based alert.
 *
 * @param alertName        Name that the alert will be created with
 * @param cloudwatchSource Which CloudWatch Grafana datasource to use
 * @param dashboardPanelId Specific panel to deep link to that is specific to this alert
 * @param dashboardUri     Grafana uri to link to. This should just be the uri path and not include the domain
 * @param dimensions       Which CloudWatch dimensions to filter the metric on
 * @param integrations     Which PagerDuty integrations to direct this alert to
 * @param metricName       Which CloudWatch metric to filter the alert on
 * @param namespace        Which CloudWatch service namespace to filter the alert on
 * @param operator         Whether to evaluate the metric as greater than or less than
 * @param reducerFunction  Function to use when manipulate data returned from query
 * @param runbookUrl       Runbook for when this alert fires
 * @param severity         The severity of this alert. E.g. Warning or Critical
 * @param summary          The description to populate in PagerDuty when the alert fires
 * @param teamName         All alerts are prefixed with the team name
 * @param thresholds       Trigger point for each environment
 */
case class CustomCloudWatchMetricAlert(
                                        alertName: String,
                                        cloudwatchSource: CloudWatchSource,
                                        dashboardPanelId: Option[Int],
                                        dashboardUri: Option[String],
                                        dimensions: Map[String, String],
                                        integrations: Seq[String],
                                        metricName: String,
                                        namespace: String,
                                        operator: EvaluationOperator,
                                        reducerFunction: Option[ReducerFunction] = Some(ReducerFunction.LAST),
                                        runbookUrl: Option[String],
                                        severity: AlertSeverity,
                                        summary: String,
                                        teamName: String,
                                        thresholds: EnvironmentThresholds
                                      ) extends CustomAlert