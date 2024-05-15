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

/** All microservices are deployed to MDTP inside docker containers. If the docker container runs out of memory then the container will be killed with
  * an out-of-memory exception. This alert will notify when a specified number of containers are killed within a 15-minute window.
  *
  * @param count
  *   The number of container kills to alert on
  * @param alertingPlatform
  *   The platform this alert will target. We are migrating towards Grafana and away from Sensu
  */
case class ContainerKillThreshold(
    count: Int = Int.MaxValue,
    alertingPlatform: AlertingPlatform = AlertingPlatform.Default
)
