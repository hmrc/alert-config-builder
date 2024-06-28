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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.alertconfig.builder.Environment

class EnvironmentThresholdsSpec extends AnyWordSpec with Matchers {

  "EnvironmentThresholds" should {

    "correctly set thresholds for all non-production environments with Int values" in {
      val thresholds = EnvironmentThresholds.forAllNonProdEnvironments(9)

      thresholds.development shouldEqual Some(IntThreshold(9))
      thresholds.externaltest shouldEqual None
      thresholds.integration shouldEqual Some(IntThreshold(9))
      thresholds.management shouldEqual Some(IntThreshold(9))
      thresholds.qa shouldEqual Some(IntThreshold(9))
      thresholds.staging shouldEqual Some(IntThreshold(9))
      thresholds.production shouldEqual None
    }

    "correctly set thresholds for all non-production environments with Double values" in {
      val thresholds = EnvironmentThresholds.forAllNonProdEnvironments(9.5)

      thresholds.development shouldEqual Some(DoubleThreshold(9.5))
      thresholds.externaltest shouldEqual None
      thresholds.integration shouldEqual Some(DoubleThreshold(9.5))
      thresholds.management shouldEqual Some(DoubleThreshold(9.5))
      thresholds.qa shouldEqual Some(DoubleThreshold(9.5))
      thresholds.staging shouldEqual Some(DoubleThreshold(9.5))
      thresholds.production shouldEqual None
    }

    "verify that an environment is defined when using Int values" in {
      val thresholds = EnvironmentThresholds.forAllNonProdEnvironments(9)

      thresholds.isEnvironmentDefined(Environment.Development) shouldEqual true
      thresholds.isEnvironmentDefined(Environment.Production) shouldEqual false
    }

    "verify that an environment is defined when using Double values" in {
      val thresholds = EnvironmentThresholds.forAllNonProdEnvironments(9.5)

      thresholds.isEnvironmentDefined(Environment.Staging) shouldEqual true
      thresholds.isEnvironmentDefined(Environment.Production) shouldEqual false
    }

    "remove all other environment thresholds except the specified one" in {
      val thresholds = EnvironmentThresholds(
        development = Some(IntThreshold(9)),
        externaltest = Some(DoubleThreshold(9.5)),
        integration = Some(IntThreshold(8)),
        management = Some(DoubleThreshold(8.5)),
        production = Some(IntThreshold(7)),
        qa = Some(DoubleThreshold(7.5)),
        staging = Some(IntThreshold(6))
      )

      val updatedThresholds = thresholds.removeAllOtherEnvironmentThresholds(Environment.Integration)

      updatedThresholds.development shouldEqual None
      updatedThresholds.externaltest shouldEqual None
      updatedThresholds.integration shouldEqual Some(IntThreshold(8))
      updatedThresholds.management shouldEqual None
      updatedThresholds.production shouldEqual None
      updatedThresholds.qa shouldEqual None
      updatedThresholds.staging shouldEqual None
    }
  }

}
