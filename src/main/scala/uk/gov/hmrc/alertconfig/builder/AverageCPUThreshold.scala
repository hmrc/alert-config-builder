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

/**
 * This alert will notify when the average CPU used by all instances of your microservice exceeds a given threshold within a 5-minute window.
 * @param count The average percentage CPU used by all instances of your microservice
 * @param alertingPlatform The platform this alert will target. We are migrating towards Grafana and away from Sensu
 */
case class AverageCPUThreshold(
    /**
     * The average percentage CPU used by all instances of your microservice
     */
    count: Int = Int.MaxValue,

    /**
     * The platform this alert will target. We are migrating towards Grafana and away from Sensu
     */
    alertingPlatform: AlertingPlatform = AlertingPlatform.Default
)
