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
 * Generate custom alerts that are based on logs in Elasticsearch
 *
 * @param logMessage The exact string that you are searching for
 * @param severity   The severity of this alert.
 * @param thresholds Trigger point for each environment
 * @param handlers   Which PagerDuty integrations to direct this alert to
 */
// TODO Later figure out what's needed
case class CustomLogAlert(
                           logMessage: String,
                           operator: EvaluationOperator,
                           severity: AlertSeverity,
                           thresholds: EnvironmentThresholds,
                           integrations: Seq[String]
                         ) extends CustomAlert
