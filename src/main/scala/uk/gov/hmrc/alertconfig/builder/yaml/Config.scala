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

package uk.gov.hmrc.alertconfig.builder.yaml

case class TopLevelConfig(services: Seq[ServiceConfig])

case class ServiceConfig(service: String, alerts: Alerts, pagerduty: Seq[PagerDuty])

case class Alerts(
    averageCPUThreshold: Option[YamlAverageCPUThresholdAlert] = None,
    containerKillThreshold: Option[YamlContainerKillThresholdAlert] = None,
    errorsLoggedThreshold: Option[YamlErrorsLoggedThresholdAlert] = None,
    exceptionThreshold: Option[YamlExceptionThresholdAlert] = None,
    logMessageThresholds: Option[Seq[YamlLogMessageThresholdAlert]] = None,
    http5xxThreshold: Option[YamlHttp5xxThresholdAlert] = None,
    http5xxPercentThreshold: Option[YamlHttp5xxPercentThresholdAlert] = None,
    httpAbsolutePercentSplitThreshold: Option[Seq[YamlHttpAbsolutePercentSplitThresholdAlert]] = None,
    httpAbsolutePercentSplitDownstreamHodThreshold: Option[Seq[YamlHttpAbsolutePercentSplitDownstreamHodThresholdAlert]] = None,
    httpStatusPercentThresholds: Option[Seq[YamlHttpStatusPercentThresholdAlert]] = None,
    httpStatusThresholds: Option[Seq[YamlHttpStatusThresholdAlert]] = None,
    httpTrafficThresholds: Option[Seq[YamlHttpTrafficThresholdAlert]] = None,
    totalHttpRequestThreshold: Option[YamlTotalHttpRequestThresholdAlert] = None,
    metricsThresholds: Option[Seq[YamlMetricsThresholdAlert]] = None,
    http90PercentileResponseTimeThreshold: Option[Seq[YamlHttp90PercentileResponseTimeThresholdAlert]] = None
)

case class PagerDuty(
    // name: String,
    integrationKeyName: String
    // slackChannel: String
)
