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

case class YamlAverageCPUThresholdAlert(
  count: Int
)

case class YamlContainerKillThresholdAlert(
  count: Int
)

case class YamlErrorsLoggedThresholdAlert(
  count: Int
)

case class YamlExceptionThresholdAlert(
  count: Int,
  severity: String
)

case class YamlHttp5xxPercentThresholdAlert(
  percentage: Double,
  severity: String
)

case class YamlHttp5xxThresholdAlert(
  count: Int,
  severity: String
)

case class YamlHttpAbsolutePercentSplitThresholdAlert(
  percentThreshold: Double,
  crossover: Int,
  absoluteThreshold: Int,
  hysteresis: Double,
  excludeSpikes: Int,
  errorFilter: String,
  severity: String
)

case class YamlHttpStatusThresholdAlert(
  count: Int = 1,
  httpMethod: String,
  httpStatus: Int,
  severity: String
)

case class YamlHttpStatusPercentThresholdAlert(
  percentage: Double,
  httpMethod: String,
  httpStatus: Int,
  severity: String
)

case class YamlLogMessageThresholdAlert(
  count: Int,
  lessThanMode: Boolean,
  message: String,
  severity: String
)

case class YamlHttpTrafficThresholdAlert(
  count: Int,
  maxMinutesBelowThreshold: Int,
  severity: String
)

case class YamlMetricsThresholdAlert(
  count: Double,
  name: String,
  query: String,
  severity: String,
  invert: Boolean
)

case class YamlTotalHttpRequestThresholdAlert(
  count: Int
)

case class YamlHttp90PercentileResponseTimeThresholdAlert(
  timePeriod: Int,
  count: Int,
  severity: String
)
