/*
 * Copyright 2023 HM Revenue & Customs
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

/**
 *This alert will notify when your microservice throws a specified number of exceptions, at log level ERROR, within a 15-minute window.
 * @param count The number of exceptions thrown that this alert will trigger on
 * @param severity Whether to raise the alert as critical or warning
 * @param alertingPlatform The platform this alert will target. We are migrating towards Grafana and away from Sensu
 */
case class ExceptionThreshold(
    count: Int = 2,
    severity: AlertSeverity = AlertSeverity.Critical,
    alertingPlatform: AlertingPlatform = AlertingPlatform.Default
)

object ExceptionThresholdProtocol {
  import DefaultJsonProtocol._

  implicit val thresholdFormat: JsonFormat[ExceptionThreshold] = {
    implicit val asf: JsonFormat[AlertSeverity] = alertSeverityFormat
    jsonFormat3(ExceptionThreshold)
  }

}
