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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object YamlWriter {

  val mapper: ObjectMapper = new ObjectMapper(
    new YAMLFactory()
      .disable(Feature.WRITE_DOC_START_MARKER)
      .disable(Feature.SPLIT_LINES)
      .enable(Feature.MINIMIZE_QUOTES)
      .enable(Feature.INDENT_ARRAYS_WITH_INDICATOR)
  )
    .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
    .registerModule(DefaultScalaModule)

}
