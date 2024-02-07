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

package uk.gov.hmrc.alertconfig.builder


sealed trait AlertType

object AlertType {

  object AverageCPUThreshold extends AlertType
  object ContainerKillThreshold extends AlertType
  object ErrorsLoggedThreshold extends AlertType
  object ExceptionThreshold extends AlertType
  object Http5xxPercentThreshold extends AlertType
  object Http5xxThreshold extends AlertType
  object HttpStatusPercentThreshold extends AlertType
  object HttpStatusThreshold extends AlertType
  object HttpTrafficThreshold extends AlertType
  object LogMessageThreshold extends AlertType
  object MetricsThreshold extends AlertType
  object TotalHttpRequestThreshold extends AlertType
}

object GrafanaMigration {

  val config = Map(
    Environment.Integration -> Map(
      AlertType.AverageCPUThreshold -> AlertingPlatform.Grafana,
      AlertType.ContainerKillThreshold -> AlertingPlatform.Grafana,
      AlertType.ErrorsLoggedThreshold -> AlertingPlatform.Grafana,
      AlertType.ExceptionThreshold -> AlertingPlatform.Grafana,
      AlertType.Http5xxPercentThreshold -> AlertingPlatform.Grafana,
      AlertType.Http5xxThreshold -> AlertingPlatform.Grafana,
      AlertType.HttpStatusPercentThreshold -> AlertingPlatform.Grafana,
      AlertType.HttpStatusThreshold -> AlertingPlatform.Grafana,
      AlertType.HttpTrafficThreshold -> AlertingPlatform.Grafana,
      AlertType.LogMessageThreshold -> AlertingPlatform.Grafana,
      AlertType.MetricsThreshold -> AlertingPlatform.Grafana,
      AlertType.TotalHttpRequestThreshold -> AlertingPlatform.Grafana
    ),

    Environment.Development -> Map(
      AlertType.AverageCPUThreshold -> AlertingPlatform.Sensu,
      AlertType.ContainerKillThreshold -> AlertingPlatform.Sensu,
      AlertType.ErrorsLoggedThreshold -> AlertingPlatform.Sensu,
      AlertType.ExceptionThreshold -> AlertingPlatform.Sensu,
      AlertType.Http5xxPercentThreshold -> AlertingPlatform.Sensu,
      AlertType.Http5xxThreshold -> AlertingPlatform.Sensu,
      AlertType.HttpStatusPercentThreshold -> AlertingPlatform.Sensu,
      AlertType.HttpStatusThreshold -> AlertingPlatform.Sensu,
      AlertType.HttpTrafficThreshold -> AlertingPlatform.Sensu,
      AlertType.LogMessageThreshold -> AlertingPlatform.Sensu,
      AlertType.MetricsThreshold -> AlertingPlatform.Sensu,
      AlertType.TotalHttpRequestThreshold -> AlertingPlatform.Sensu
    ),

    Environment.Qa -> Map(
      AlertType.AverageCPUThreshold -> AlertingPlatform.Sensu,
      AlertType.ContainerKillThreshold -> AlertingPlatform.Sensu,
      AlertType.ErrorsLoggedThreshold -> AlertingPlatform.Sensu,
      AlertType.ExceptionThreshold -> AlertingPlatform.Sensu,
      AlertType.Http5xxPercentThreshold -> AlertingPlatform.Sensu,
      AlertType.Http5xxThreshold -> AlertingPlatform.Sensu,
      AlertType.HttpStatusPercentThreshold -> AlertingPlatform.Sensu,
      AlertType.HttpStatusThreshold -> AlertingPlatform.Sensu,
      AlertType.HttpTrafficThreshold -> AlertingPlatform.Sensu,
      AlertType.LogMessageThreshold -> AlertingPlatform.Sensu,
      AlertType.MetricsThreshold -> AlertingPlatform.Sensu,
      AlertType.TotalHttpRequestThreshold -> AlertingPlatform.Sensu
    ),

    Environment.Staging -> Map(
      AlertType.AverageCPUThreshold -> AlertingPlatform.Sensu,
      AlertType.ContainerKillThreshold -> AlertingPlatform.Sensu,
      AlertType.ErrorsLoggedThreshold -> AlertingPlatform.Sensu,
      AlertType.ExceptionThreshold -> AlertingPlatform.Sensu,
      AlertType.Http5xxPercentThreshold -> AlertingPlatform.Sensu,
      AlertType.Http5xxThreshold -> AlertingPlatform.Sensu,
      AlertType.HttpStatusPercentThreshold -> AlertingPlatform.Sensu,
      AlertType.HttpStatusThreshold -> AlertingPlatform.Sensu,
      AlertType.HttpTrafficThreshold -> AlertingPlatform.Sensu,
      AlertType.LogMessageThreshold -> AlertingPlatform.Sensu,
      AlertType.MetricsThreshold -> AlertingPlatform.Sensu,
      AlertType.TotalHttpRequestThreshold -> AlertingPlatform.Sensu
    ),

    Environment.ExternalTest -> Map(
      AlertType.AverageCPUThreshold -> AlertingPlatform.Sensu,
      AlertType.ContainerKillThreshold -> AlertingPlatform.Sensu,
      AlertType.ErrorsLoggedThreshold -> AlertingPlatform.Sensu,
      AlertType.ExceptionThreshold -> AlertingPlatform.Sensu,
      AlertType.Http5xxPercentThreshold -> AlertingPlatform.Sensu,
      AlertType.Http5xxThreshold -> AlertingPlatform.Sensu,
      AlertType.HttpStatusPercentThreshold -> AlertingPlatform.Sensu,
      AlertType.HttpStatusThreshold -> AlertingPlatform.Sensu,
      AlertType.HttpTrafficThreshold -> AlertingPlatform.Sensu,
      AlertType.LogMessageThreshold -> AlertingPlatform.Sensu,
      AlertType.MetricsThreshold -> AlertingPlatform.Sensu,
      AlertType.TotalHttpRequestThreshold -> AlertingPlatform.Sensu
    ),

    Environment.Production -> Map(
      AlertType.AverageCPUThreshold -> AlertingPlatform.Sensu,
      AlertType.ContainerKillThreshold -> AlertingPlatform.Sensu,
      AlertType.ErrorsLoggedThreshold -> AlertingPlatform.Sensu,
      AlertType.ExceptionThreshold -> AlertingPlatform.Sensu,
      AlertType.Http5xxPercentThreshold -> AlertingPlatform.Sensu,
      AlertType.Http5xxThreshold -> AlertingPlatform.Sensu,
      AlertType.HttpStatusPercentThreshold -> AlertingPlatform.Sensu,
      AlertType.HttpStatusThreshold -> AlertingPlatform.Sensu,
      AlertType.HttpTrafficThreshold -> AlertingPlatform.Sensu,
      AlertType.LogMessageThreshold -> AlertingPlatform.Sensu,
      AlertType.MetricsThreshold -> AlertingPlatform.Sensu,
      AlertType.TotalHttpRequestThreshold -> AlertingPlatform.Sensu
    )
  )
    def isGrafanaEnabled(alertingPlatform: AlertingPlatform, currentEnvironment: Environment, alertType: AlertType): Boolean = {
      alertingPlatform match {
        case AlertingPlatform.Sensu => false
        case AlertingPlatform.Grafana => true
        case _ => config(currentEnvironment)(alertType) == AlertingPlatform.Grafana
      }
    }
}
