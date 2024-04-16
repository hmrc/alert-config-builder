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

/**
 * Graphite metric based alert.
 *
 * @param alertName     Name that the alert will be created with
 * @param dashboardUrl  Grafana or Kibana dashboard to link to
 * @param integrations  Which PagerDuty integrations to direct this alert to
 * @param operator      Whether to evaluate the metric as greater than or less than
 * @param query         Graphite query you're running
 * @param ruleGroupName Which Grafana Alerting rule group this belongs to
 * @param runbookUrl    Runbook for when this alert fires
 * @param severity      The severity of this alert. E.g. Warning or Critical
 * @param summary       The description to populate in PagerDuty when the alert fires
 * @param thresholds    Trigger point for each environment
 */
case class CustomMetricAlert(
                              alertName: String,
                              dashboardUrl: Option[String],
                              integrations: Seq[String],
                              operator: EvaluationOperator,
                              query: String,
                              ruleGroupName: String,
                              runbookUrl: Option[String],
                              severity: AlertSeverity,
                              summary: String,
                              thresholds: EnvironmentThresholds,
                            ) extends CustomAlert