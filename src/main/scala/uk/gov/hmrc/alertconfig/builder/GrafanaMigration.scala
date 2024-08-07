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

  object AverageCPUThreshold                                extends AlertType
  object ContainerKillThreshold                             extends AlertType
  object ErrorsLoggedThreshold                              extends AlertType
  object ExceptionThreshold                                 extends AlertType
  object Http5xxPercentThreshold                            extends AlertType
  object Http5xxThreshold                                   extends AlertType
  object HttpAbsolutePercentSplitThreshold                  extends AlertType
  object HttpAbsolutePercentSplitDownstreamHodThreshold     extends AlertType
  object HttpAbsolutePercentSplitDownstreamServiceThreshold extends AlertType
  object HttpEndpointAlert                                  extends AlertType
  object HttpStatusPercentThreshold                         extends AlertType
  object HttpStatusThreshold                                extends AlertType
  object HttpTrafficThreshold                               extends AlertType
  object LogMessageThreshold                                extends AlertType
  object MetricsThreshold                                   extends AlertType
  object TotalHttpRequestThreshold                          extends AlertType
  object Http90PercentileResponseTimeThreshold              extends AlertType
}

/** This class determines which standard alerts go where in each environment.
  *
  * This is so that the Telemetry team can safely migrate alerts gradually rather than "big bang"ing them out
  */
object GrafanaMigration {

  val config = Map(
    Environment.Integration -> Map(
      AlertType.AverageCPUThreshold                                -> AlertingPlatform.Grafana,
      AlertType.ContainerKillThreshold                             -> AlertingPlatform.Grafana,
      AlertType.ErrorsLoggedThreshold                              -> AlertingPlatform.Grafana,
      AlertType.ExceptionThreshold                                 -> AlertingPlatform.Grafana,
      AlertType.Http5xxPercentThreshold                            -> AlertingPlatform.Grafana,
      AlertType.Http5xxThreshold                                   -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitThreshold                  -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitDownstreamHodThreshold     -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitDownstreamServiceThreshold -> AlertingPlatform.Grafana,
      AlertType.HttpEndpointAlert                                  -> AlertingPlatform.Grafana,
      AlertType.HttpStatusPercentThreshold                         -> AlertingPlatform.Grafana,
      AlertType.HttpStatusThreshold                                -> AlertingPlatform.Grafana,
      AlertType.HttpTrafficThreshold                               -> AlertingPlatform.Grafana,
      AlertType.LogMessageThreshold                                -> AlertingPlatform.Grafana,
      AlertType.MetricsThreshold                                   -> AlertingPlatform.Grafana,
      AlertType.TotalHttpRequestThreshold                          -> AlertingPlatform.Grafana,
      AlertType.Http90PercentileResponseTimeThreshold              -> AlertingPlatform.Grafana
    ),
    Environment.Development -> Map(
      AlertType.AverageCPUThreshold                                -> AlertingPlatform.Grafana,
      AlertType.ContainerKillThreshold                             -> AlertingPlatform.Grafana,
      AlertType.ErrorsLoggedThreshold                              -> AlertingPlatform.Grafana,
      AlertType.ExceptionThreshold                                 -> AlertingPlatform.Grafana,
      AlertType.Http5xxPercentThreshold                            -> AlertingPlatform.Grafana,
      AlertType.Http5xxThreshold                                   -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitThreshold                  -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitDownstreamHodThreshold     -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitDownstreamServiceThreshold -> AlertingPlatform.Grafana,
      AlertType.HttpEndpointAlert                                  -> AlertingPlatform.Grafana,
      AlertType.HttpStatusPercentThreshold                         -> AlertingPlatform.Grafana,
      AlertType.HttpStatusThreshold                                -> AlertingPlatform.Grafana,
      AlertType.HttpTrafficThreshold                               -> AlertingPlatform.Grafana,
      AlertType.LogMessageThreshold                                -> AlertingPlatform.Grafana,
      AlertType.MetricsThreshold                                   -> AlertingPlatform.Grafana,
      AlertType.TotalHttpRequestThreshold                          -> AlertingPlatform.Grafana,
      AlertType.Http90PercentileResponseTimeThreshold              -> AlertingPlatform.Grafana
    ),
    Environment.Qa -> Map(
      AlertType.AverageCPUThreshold                                -> AlertingPlatform.Grafana,
      AlertType.ContainerKillThreshold                             -> AlertingPlatform.Grafana,
      AlertType.ErrorsLoggedThreshold                              -> AlertingPlatform.Grafana,
      AlertType.ExceptionThreshold                                 -> AlertingPlatform.Grafana,
      AlertType.Http5xxPercentThreshold                            -> AlertingPlatform.Grafana,
      AlertType.Http5xxThreshold                                   -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitThreshold                  -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitDownstreamHodThreshold     -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitDownstreamServiceThreshold -> AlertingPlatform.Grafana,
      AlertType.HttpEndpointAlert                                  -> AlertingPlatform.Grafana,
      AlertType.HttpStatusPercentThreshold                         -> AlertingPlatform.Grafana,
      AlertType.HttpStatusThreshold                                -> AlertingPlatform.Grafana,
      AlertType.HttpTrafficThreshold                               -> AlertingPlatform.Grafana,
      AlertType.LogMessageThreshold                                -> AlertingPlatform.Grafana,
      AlertType.MetricsThreshold                                   -> AlertingPlatform.Grafana,
      AlertType.TotalHttpRequestThreshold                          -> AlertingPlatform.Grafana,
      AlertType.Http90PercentileResponseTimeThreshold              -> AlertingPlatform.Grafana
    ),
    Environment.Staging -> Map(
      AlertType.AverageCPUThreshold                                -> AlertingPlatform.Grafana,
      AlertType.ContainerKillThreshold                             -> AlertingPlatform.Grafana,
      AlertType.ErrorsLoggedThreshold                              -> AlertingPlatform.Grafana,
      AlertType.ExceptionThreshold                                 -> AlertingPlatform.Grafana,
      AlertType.Http5xxPercentThreshold                            -> AlertingPlatform.Grafana,
      AlertType.Http5xxThreshold                                   -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitThreshold                  -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitDownstreamHodThreshold     -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitDownstreamServiceThreshold -> AlertingPlatform.Grafana,
      AlertType.HttpEndpointAlert                                  -> AlertingPlatform.Grafana,
      AlertType.HttpStatusPercentThreshold                         -> AlertingPlatform.Grafana,
      AlertType.HttpStatusThreshold                                -> AlertingPlatform.Grafana,
      AlertType.HttpTrafficThreshold                               -> AlertingPlatform.Grafana,
      AlertType.LogMessageThreshold                                -> AlertingPlatform.Grafana,
      AlertType.MetricsThreshold                                   -> AlertingPlatform.Grafana,
      AlertType.TotalHttpRequestThreshold                          -> AlertingPlatform.Grafana,
      AlertType.Http90PercentileResponseTimeThreshold              -> AlertingPlatform.Grafana
    ),
    Environment.ExternalTest -> Map(
      AlertType.AverageCPUThreshold                                -> AlertingPlatform.Grafana,
      AlertType.ContainerKillThreshold                             -> AlertingPlatform.Grafana,
      AlertType.ErrorsLoggedThreshold                              -> AlertingPlatform.Grafana,
      AlertType.ExceptionThreshold                                 -> AlertingPlatform.Grafana,
      AlertType.Http5xxPercentThreshold                            -> AlertingPlatform.Grafana,
      AlertType.Http5xxThreshold                                   -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitThreshold                  -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitDownstreamHodThreshold     -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitDownstreamServiceThreshold -> AlertingPlatform.Grafana,
      AlertType.HttpEndpointAlert                                  -> AlertingPlatform.Grafana,
      AlertType.HttpStatusPercentThreshold                         -> AlertingPlatform.Grafana,
      AlertType.HttpStatusThreshold                                -> AlertingPlatform.Grafana,
      AlertType.HttpTrafficThreshold                               -> AlertingPlatform.Grafana,
      AlertType.LogMessageThreshold                                -> AlertingPlatform.Grafana,
      AlertType.MetricsThreshold                                   -> AlertingPlatform.Grafana,
      AlertType.TotalHttpRequestThreshold                          -> AlertingPlatform.Grafana,
      AlertType.Http90PercentileResponseTimeThreshold              -> AlertingPlatform.Grafana
    ),
    Environment.Production -> Map(
      AlertType.AverageCPUThreshold                                -> AlertingPlatform.Grafana,
      AlertType.ContainerKillThreshold                             -> AlertingPlatform.Grafana,
      AlertType.ErrorsLoggedThreshold                              -> AlertingPlatform.Grafana,
      AlertType.ExceptionThreshold                                 -> AlertingPlatform.Grafana,
      AlertType.Http5xxPercentThreshold                            -> AlertingPlatform.Grafana,
      AlertType.Http5xxThreshold                                   -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitThreshold                  -> AlertingPlatform.Grafana,
      AlertType.HttpStatusPercentThreshold                         -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitDownstreamHodThreshold     -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitDownstreamServiceThreshold -> AlertingPlatform.Grafana,
      AlertType.HttpEndpointAlert                                  -> AlertingPlatform.Grafana,
      AlertType.HttpStatusThreshold                                -> AlertingPlatform.Grafana,
      AlertType.HttpTrafficThreshold                               -> AlertingPlatform.Grafana,
      AlertType.LogMessageThreshold                                -> AlertingPlatform.Grafana,
      AlertType.MetricsThreshold                                   -> AlertingPlatform.Sensu,
      AlertType.TotalHttpRequestThreshold                          -> AlertingPlatform.Grafana,
      AlertType.Http90PercentileResponseTimeThreshold              -> AlertingPlatform.Grafana
    ),
    Environment.Management -> Map(
      AlertType.AverageCPUThreshold                                -> AlertingPlatform.Grafana,
      AlertType.ContainerKillThreshold                             -> AlertingPlatform.Grafana,
      AlertType.ErrorsLoggedThreshold                              -> AlertingPlatform.Grafana,
      AlertType.ExceptionThreshold                                 -> AlertingPlatform.Grafana,
      AlertType.Http5xxPercentThreshold                            -> AlertingPlatform.Grafana,
      AlertType.Http5xxThreshold                                   -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitThreshold                  -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitDownstreamHodThreshold     -> AlertingPlatform.Grafana,
      AlertType.HttpAbsolutePercentSplitDownstreamServiceThreshold -> AlertingPlatform.Grafana,
      AlertType.HttpEndpointAlert                                  -> AlertingPlatform.Grafana,
      AlertType.HttpStatusPercentThreshold                         -> AlertingPlatform.Grafana,
      AlertType.HttpStatusThreshold                                -> AlertingPlatform.Grafana,
      AlertType.HttpTrafficThreshold                               -> AlertingPlatform.Grafana,
      AlertType.LogMessageThreshold                                -> AlertingPlatform.Grafana,
      AlertType.MetricsThreshold                                   -> AlertingPlatform.Grafana,
      AlertType.TotalHttpRequestThreshold                          -> AlertingPlatform.Grafana,
      AlertType.Http90PercentileResponseTimeThreshold              -> AlertingPlatform.Grafana
    )
  )

  /** @param alertingPlatform
    *   Alerting platform to test for Grafana being enabled
    * @param currentEnvironment
    *   env to test for Grafana being enabled
    * @param alertType
    *   alert type to test for Grafana being enabled
    * @return
    *   True if the alerting platform matches the data { alertingPlatform, currentEnvironment, alertType }. All 3 must be matches for the stated alert
    *   in the stated env at the states severity to be Grafana-ised.
    */
  def isGrafanaEnabled(alertingPlatform: AlertingPlatform, currentEnvironment: Environment, alertType: AlertType): Boolean = {
    alertingPlatform match {
      case AlertingPlatform.Sensu   => false
      case AlertingPlatform.Grafana => true
      case _                        => config(currentEnvironment)(alertType) == AlertingPlatform.Grafana
    }
  }

}
