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

object TimeRangeAsMinutes {
  type TimeRangeAsMinutes = Int

  val DEFAULT           = 15
  val ONE_MINUTE        = 1
  val TWO_MINUTES       = 2
  val THREE_MINUTES     = 3
  val FOUR_MINUTES      = 4
  val FIVE_MINUTES      = 5
  val SIX_MINUTES       = 6
  val EIGHT_MINUTES     = 8
  val TEN_MINUTES       = 10
  val FIFTEEN_MINUTES   = 15
  val SIXTEEN_MINUTES   = 16
  val TWENTY_MINUTES    = 20
  val THIRTY_MINUTES    = 30
  val ONE_HOUR          = 1 * 60
  val NINETY_MINUTES    = 90
  val TWO_HOURS         = 2 * 60
  val TWELVE_HOURS      = 12 * 60
  val TWENTY_FOUR_HOURS = 24 * 60
}
