package uk.gov.hmrc.alertconfig.builder.custom

import uk.gov.hmrc.alertconfig.builder.Severity

//class CustomAlertConfigBuilder(
//                                // serviceName: String,
//                                TODO Check do we need them to provide some sort of a team name or something?
//                                // handlers: Seq[String],
//                                TODO Will these be specific to each alert so not at the top level?
//                                customAlerts: Seq[CustomAlert]
//                              )

sealed trait EvaluationOperator
object LessThan extends EvaluationOperator
object GreaterThan extends EvaluationOperator

sealed trait CustomAlert

// TODO Metrics or Metric?
case class CustomMetricsAlert(
                               // TODO Re-order
                               name: String,
                               summary: String,
                               dashboardUrl: Option[String],
                               query: String,
                               ruleGroupName: String,
                               runbookUrl: Option[String],
                               severity: Severity,
                               operator: EvaluationOperator,
                               thresholds: EnvironmentThresholds,
                               handlers: Seq[String]
                             ) extends CustomAlert

/**
 * Generate custom alerts that are based on logs in Elasticsearch
 *
 * @param logMessage The exact string that you are searching for
 * @param severity The severity of this alert.
 * @param thresholds Trigger point for each environment
 * @param handlers Which PagerDuty integrations to direct this alert to
 */
case class CustomLogAlert(
                           logMessage: String,
                           operator: EvaluationOperator,
                           severity: Severity,
                           foobar: Boolean,
                           thresholds: EnvironmentThresholds,
                           handlers: Seq[String] // TODO Should these still be called handlers?
                         ) extends CustomAlert // TODO Later

case class EnvironmentThresholds(
                                  development: Option[Int] = None,
                                  externalTest: Option[Int] = None,
                                  integration: Option[Int] = None,
                                  management: Option[Int] = None,
                                  production: Option[Int] = None,
                                  qa: Option[Int] = None,
                                  staging: Option[Int] = None
                                )

trait CustomAlertConfig {
  def customAlerts: Seq[CustomAlert]
}


object Telemetry extends CustomAlertConfig {
  override def customAlerts: Seq[CustomAlert] = Seq(
    CustomMetricsAlert(
      name = "Some Metric Check",
      summary = "???",
      dashboardUrl = Some("http://something.com"),
      query = "sum(some.metric.path)",
      ruleGroupName = "telemetry-custom-metrics",
      runbookUrl = Some("www.google.com"),
      severity = Severity.Warning,
      thresholds = EnvironmentThresholds(
        qa = Some(12)
      ),
      operator = GreaterThan,
      handlers = Seq("labs-team-telemetry")
    ),
    CustomLogAlert(
      logMessage = "THE STRING I'M LOOKING FOR",
      severity = Severity.Critical,
      thresholds = EnvironmentThresholds(
        staging = Some(5)
      ),
      operator = GreaterThan,
      handlers = Seq("labs-team-telemetry")
    ),
    CustomLogAlert(
      logMessage = "ANOTHER STRING I'M CONSTANTLY LOOKING FOR",
      severity = Severity.Warning,
      thresholds = EnvironmentThresholds(
        management = Some(10)
      ),
      operator = LessThan,
      handlers = Seq("labs-team-telemetry")
    )
  )
}





