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

case class YAMLAverageCPUThresholdAlert(
                                         count: Int
                                       )

case class YAMLContainerKillThresholdAlert(
                                            count: Int
                                          )

case class YAMLErrorsLoggedThresholdAlert(
                                           count: Int
                                         )

case class YAMLExceptionThresholdAlert(
                                        count: Int,
                                        severity: String
                                      )

case class YAMLHttp5xxPercentThresholdAlert(
                                             percentage: Double,
                                             severity: String
                                           )

case class YAMLHttp5xxThresholdAlert(
                                      count: Int,
                                      severity: String
                                    )

case class YAMLHttpStatusThresholdAlert(
                                         count: Int = 1,
                                         httpMethod: String,
                                         httpStatus: Int,
                                         severity: String
                                       )

case class YAMLHttpStatusPercentThresholdAlert(
                                                percentage: Double,
                                                httpMethod: String,
                                                httpStatus: Int,
                                                severity: String
                                              )

case class YAMLLogMessageThresholdAlert(
                                         count: Int,
                                         lessThanMode: Boolean,
                                         message: String,
                                         severity: String
                                       )

case class YAMLHttpTrafficThresholdAlert(
                                          count: Int,
                                          maxMinutesBelowThreshold: Int,
                                          severity: String
                                        )

case class YAMLMetricsThresholdAlert(
                                      count: Double,
                                      name: String,
                                      query: String,
                                      severity: String,
                                      invert: Boolean
                                    )

case class YAMLTotalHttpRequestThresholdAlert(
                                               count: Int
                                             )
