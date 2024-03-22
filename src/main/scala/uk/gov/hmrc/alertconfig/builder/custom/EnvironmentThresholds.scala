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

/**
 * Define thresholds for any environments you want this custom
 * alert to be active in.
 */
case class EnvironmentThresholds(
                                  development: Option[Int] = None,
                                  externalTest: Option[Int] = None,
                                  integration: Option[Int] = None,
                                  management: Option[Int] = None,
                                  production: Option[Int] = None,
                                  qa: Option[Int] = None,
                                  staging: Option[Int] = None
                                )
