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
 * An enumeration of sorts that encapsulates all the possible alert severities supported
 * by alert-config
 */
sealed trait AlertSeverity

object AlertSeverity {
  object Info     extends AlertSeverity { override def toString: String = "info"  }
  object Warning  extends AlertSeverity { override def toString: String = "warning"  }
  object Critical extends AlertSeverity { override def toString: String = "critical" }
}
