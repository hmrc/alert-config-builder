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

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.constructor.AbstractConstruct
import org.yaml.snakeyaml.nodes.{Node, ScalarNode, Tag}
import org.yaml.snakeyaml.representer.{Represent, Representer}
import org.yaml.snakeyaml.serializer.Serializer


object EvaluationOperator {
  type EvaluationOperator = String

  val GREATER_THAN = "gt"
  val LESS_THAN = "lt"
}

//object EvalOperators extends Enumeration {
//  type EvalOperator = Value
//
//  val LessThan: EvalOperators.Value = Value(1, "lt")
//  val GreaterThan: EvalOperators.Value = Value(2, "gt")
//}

//class OperatorRepresent extends Represent {
//  override def representData(data: Any): Node = {
//    val operator = data.asInstanceOf[EvalOperator]
//    new ScalarNode(null, operator.toString, null, null)
//  }
//}
//class EvalOperatorRepresent(dumperOptions: DumperOptions) extends Representer(dumperOptions) {
//  override def represent(data: Any): Node = {
//    val operator = data.asInstanceOf[EvalOperators.EvalOperator]
//    representScalar(new Tag("ABDRA"), operator.toString)
//  }
//}

//
//sealed trait EvaluationOperator
//
//object LessThan extends EvaluationOperator {
//  override def toString: String = "lt"
//}
//
//case class GreaterThan extends EvaluationOperator {
//  override def toString: String = "gt"
//}
//
//
//
//class GreaterThanSerializer extends Serializer {
//
//  override def representData(data: Any): Node = {
//    val greaterThan = data.asInstanceOf[GreaterThan]
//    new ScalarNode(null, null, "gt", null)
//  }
//}
//
//def customYamlSerializer: Yaml = {
//  val options = new DumperOptions
//  val representer = new Representer
//
//  representer.addRepresenter(classOf[GreaterThan], new Represent {
//    val serializer = new GreaterThanSerializer
//
//    override def representData(data: Any): Node = {
//      serializer.representData(data)
//    }
//  })
//
//  new Yaml(representer, options)
//}
//

// TODO Create custom serialiser https://techbuddy.dev/java-snakeyaml-guide-yaml-serialization-deserialization ?
// https://stackoverflow.com/questions/54351787/how-to-serialize-fields-with-custom-names-using-snake-yaml-in-java

//class EvaluationOperatorTypeConverter() extends AbstractConstruct{
//  override def construct(node: Node): AnyRef = ???
//}