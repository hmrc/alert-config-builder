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

case class AlertConfigBuilder(
    serviceName: String,
    integrations: Seq[String] = Nil,
    errorsLoggedThreshold: Option[ErrorsLoggedThreshold] = None,
    exceptionThreshold: Option[ExceptionThreshold] = Some(ExceptionThreshold(2)),
    http5xxThreshold: Option[Http5xxThreshold] = None,
    http5xxPercentThreshold: Option[Http5xxPercentThreshold] = Some(Http5xxPercentThreshold(100.0)),
    http90PercentileResponseTimeThresholds: Seq[Http90PercentileResponseTimeThreshold] = Nil,
    httpAbsolutePercentSplitThresholds: Seq[HttpAbsolutePercentSplitThreshold] = Nil,
    httpAbsolutePercentSplitDownstreamServiceThresholds: Seq[HttpAbsolutePercentSplitDownstreamServiceThreshold] = Nil,
    httpAbsolutePercentSplitDownstreamHodThresholds: Seq[HttpAbsolutePercentSplitDownstreamHodThreshold] = Nil,
    containerKillThreshold: Option[ContainerKillThreshold] = Some(ContainerKillThreshold(1)),
    httpTrafficThresholds: Seq[HttpTrafficThreshold] = Nil,
    httpStatusThresholds: Seq[HttpStatusThreshold] = Nil,
    httpStatusPercentThresholds: Seq[HttpStatusPercentThreshold] = Nil,
    metricsThresholds: Seq[MetricsThreshold] = Nil,
    logMessageThresholds: Seq[LogMessageThreshold] = Nil,
    totalHttpRequestThreshold: Option[TotalHttpRequestThreshold] = None,
    averageCPUThreshold: Option[AverageCPUThreshold] = None,
    platformService: Boolean = false
) {

  val logger = new Logger()

  /** Set of integrations for the configuration, e.g. .withIntegrations("dass-cyclone")
    * @param integrations
    *   List of names of your pagerduty integrations. These integrations must already exist in PagerDuty; alert-config will not create then for you.
    */
  def withIntegrations(integrations: String*): AlertConfigBuilder =
    this.copy(integrations = integrations)

  /** This alert will notify when your microservice logs a specified number of logs at ERROR log level within a 15-minute window.
    *
    * @param errorsLoggedThreshold
    *   The number of logs at ERROR level that this alert will trigger on
    * @return
    * @example
    * <pre> <pre>.withErrorsLoggedThreshold(5) # alert when 5 logs at ERROR level are logged in a 15-minute window </pre> </pre>
    */
  def withErrorsLoggedThreshold(errorsLoggedThreshold: Int): AlertConfigBuilder =
    this.copy(errorsLoggedThreshold = Some(ErrorsLoggedThreshold(errorsLoggedThreshold)))

  /** This will disable the ExceptionThreshold alert which is configured with a threshold of 2 by default */
  def disableExceptionThreshold(): AlertConfigBuilder = this.copy(exceptionThreshold = None)

  /** This alert will notify when your microservice throws a specified number of exceptions, at log level ERROR, within a 15-minute window.
    * @param exceptionThreshold
    *   The number of exceptions thrown that this alert will trigger on
    * @return
    * @example
    * <pre> <pre>.withExceptionThreshold(count: Int, severity: AlertSeverity &#61; AlertSeverity.Critical) </pre> </pre>
    */
  def withExceptionThreshold(exceptionThreshold: Int, severity: AlertSeverity = AlertSeverity.Critical): AlertConfigBuilder =
    this.copy(exceptionThreshold = Some(ExceptionThreshold(exceptionThreshold, severity)))

  /** This alert will notify when the number of http responses returning a 5xx http status code exceeds a given threshold within a 15-minute window.
    *
    * @param http5xxThreshold
    *   The number of all http responses with a 5xx status code to alert on
    * @param severity
    *   Whether to raise the alert as critical or warning
    * @return
    * @example
    * <pre> <pre>.withHttp5xxThreshold(5) # alert when 5 requests return a 5xx status code in a 15-minute window .withHttp5xxThreshold(5,
   *  AlertSeverity.Warning) # raise a warning alert when 5 requests return a 5xx status code in a 15-minute window </pre> </pre>
    */
  def withHttp5xxThreshold(http5xxThreshold: Int, severity: AlertSeverity = AlertSeverity.Critical): AlertConfigBuilder =
    this.copy(http5xxThreshold = Some(Http5xxThreshold(http5xxThreshold, severity)))

  /** This will disable the Http5xxPercentThreshold alert which is configured with a threshold of 100% by default */
  def disableHttp5xxPercentThreshold(): AlertConfigBuilder = this.copy(http5xxPercentThreshold = None)

  /** This alert will notify when the percentage of http responses returning a 5xx http status code exceeds a given threshold within a 15-minute
    * window.
    *
    * This alert is enabled by default at 100%, but can be disabled by using .disableHttp5xxPercentThreshold().
    *
    * @param percentThreshold
    *   The %age of requests that must be 5xx to trigger the alarm
    * @param minimumHttp5xxCountThreshold
    *   The minimum count of 5xxs that must be present for the percentThreshold check to kick in. Optional. If you want to create a
    *   5xxPercentThreshold alert but only if you have a given count of 5xxs, this is the method to use. You want to use this parameter if, for
    *   example, you don't want to alert on just a one-off 5xx in the middle of the night Defaults to 0, which would mean the parameter has no effect.
    * @param severity
    *   How severe the alert is
    * @return
    *   Configured threshold object
    * @example
    * <pre> <pre>.withHttp5xxPercentThreshold(25.0) # alert when 25% of all requests return a 5xx status code in a 15-minute window
   *  .withHttp5xxPercentThreshold(25.0, AlertSeverity.Warning) # raise a warning alert when 25% of all requests return a 5xx status code in a
   *  15-minute window .withHttp5xxPercentThreshold(25.0, 5, AlertSeverity.Warning) # raise a warning alert when 25% of all requests return a 5xx
   *  status code in a 15-minute window, where there are at least 5 total 5xx status codes observed in the time window </pre> </pre>
    */
  def withHttp5xxPercentThreshold(percentThreshold: Double, minimumHttp5xxCountThreshold: Int = 0, severity: AlertSeverity = AlertSeverity.Critical): AlertConfigBuilder =
    if (percentThreshold > 100) {
      println(Console.CYAN + s" = ${percentThreshold}" + Console.RESET)
      throw new Exception(
        s"withHttp5xxPercentThreshold percentThreshold '${percentThreshold}' cannot be over 100% - use .disableHttp5xxPercentThreshold() to disable this alert")
    } else {
      this.copy(http5xxPercentThreshold = Some(Http5xxPercentThreshold(percentThreshold, minimumHttp5xxCountThreshold, severity)))
    }

  /** This alert will notify you when the 90th Percentile request time goes above the defined thresholds for the time period specified by the user.
    * @param threshold
    *   Object representing the response time in milliseconds above which alerts will be raised at given severities for a given time period
    * @return
    * @example
    * <pre> <pre>.withHttp90PercentileResponseTimeThreshold(Http90PercentileResponseTimeThreshold(warning &#61; Some(1000), critical &#61; Some(2000),
   *  timePeriod &#61; 7)) </pre> </pre>
    */
  def withHttp90PercentileResponseTimeThreshold(threshold: Http90PercentileResponseTimeThreshold): AlertConfigBuilder = {
    if (http90PercentileResponseTimeThresholds.nonEmpty) {
      throw new Exception("withHttp90PercentileResponseTimeThreshold has already been defined for this microservice")
    } else if (threshold.timePeriod < 0 || threshold.timePeriod > 15) {
      println(Console.CYAN + s" = ${threshold}" + Console.RESET)
      throw new Exception(
        s"withHttp90PercentileResponseTimeThreshold timePeriod '${threshold.timePeriod}' needs to be in the range 1-15 minutes (inclusive)")
    } else {
      this.copy(http90PercentileResponseTimeThresholds = Seq(threshold))
    }
  }

  /** @param threshold
    *   Object with fields percentThreshold, crossover, absoluteThreshold, hysteresis, excludeSpikes, errorFilter, severity
    * @return
    * @example
    * <pre> <pre>.withHttpAbsolutePercentSplitThreshold(HttpAbsolutePercentSplitThreshold(100.0, Int.MaxValue, 40, 1.0, 2, "status:499")) </pre> </pre>
    */
  def withHttpAbsolutePercentSplitThreshold(threshold: HttpAbsolutePercentSplitThreshold): AlertConfigBuilder =
    this.copy(httpAbsolutePercentSplitThresholds = httpAbsolutePercentSplitThresholds :+ threshold)

  /** @param threshold
    *   Object with fields percentThreshold, crossover, absoluteThreshold, hysteresis, excludeSpikes, errorFilter, target, severity
    * @return
    * @example
    * <pre> <pre>.withHttpAbsolutePercentSplitDownstreamServiceThreshold(HttpAbsolutePercentSplitDownstreamServiceThreshold(10.0, 0, -1, 1.1, 2,
    *  "status:>498", "nps-hod-service",AlertSeverity.Critical)) </pre> </pre>
    */
  def withHttpAbsolutePercentSplitDownstreamServiceThreshold(threshold: HttpAbsolutePercentSplitDownstreamServiceThreshold): AlertConfigBuilder =
    this.copy(httpAbsolutePercentSplitDownstreamServiceThresholds = httpAbsolutePercentSplitDownstreamServiceThresholds :+ threshold)

  /** @param threshold
    * Object with fields percentThreshold, crossover, absoluteThreshold, hysteresis, excludeSpikes, errorFilter, target, severity
    * @return
    * @example
    * <pre> <pre>.withHttpAbsolutePercentSplitDownstreamHodThreshold(HttpAbsolutePercentSplitDownstreamHodThreshold(10.0, 0, -1, 1.1, 2,
    *  "status:>498", "nps-hod-service",AlertSeverity.Critical)) </pre> </pre>
    */
  def withHttpAbsolutePercentSplitDownstreamHodThreshold(threshold: HttpAbsolutePercentSplitDownstreamHodThreshold): AlertConfigBuilder =
    this.copy(httpAbsolutePercentSplitDownstreamHodThresholds = httpAbsolutePercentSplitDownstreamHodThresholds :+ threshold)

  /** This alert will notify when your microservice returns a specified number of requests with a given http status code (between 400 and 599) within
    * a 15-minute window.
    * @param threshold
    *   Object with fields httpStatus, count, severity and httpMethod
    * @return
    *
    * @example
    * <pre> <pre>.withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_500, 5)) # alert when 5 occurences of status code 500 in a 15-minute
   *  window .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS(501), 5)) # alert when 5 occurences of status code 501 in a 15-minute window
   *  .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_502, 5, AlertSeverity.Warning)) # raise a warning alert when 5 occurences of status
   *  code 502 in a 15-minute window .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_503, 5, AlertSeverity.Warning, httpMethod.Post)) #
   *  raise a warning alert when 5 occurences of Post requests with response status code 503 in a 15-minute window </pre> </pre>
    */
  def withHttpStatusThreshold(threshold: HttpStatusThreshold): AlertConfigBuilder =
    this.copy(httpStatusThresholds = httpStatusThresholds :+ threshold)

  /** This alert will notify you when the total number of requests received by the microservice is below a certain threshold.
    *
    * One or both of warning and critical must be given.
    *
    * @param threshold
    * Object with fields warning, critical and maxMinutesBelowThreshold
    * @return
    * @example
    * <pre> <pre>.withHttpTrafficThreshold(HttpTrafficThreshold(10, 5, 15)) # alert with a warning when less than 10
    * requests or critical when less than 5 requests are received in a 15-minute
    * </pre>
    */
  def withHttpTrafficThreshold(threshold: HttpTrafficThreshold): AlertConfigBuilder = {
    if (httpTrafficThresholds.nonEmpty) {
      throw new Exception("withHttpTrafficThreshold has already been defined for this microservice")
    } else {
      this.copy(httpTrafficThresholds = Seq(threshold))
    }
  }

  /** This alert will notify when the percentage of http responses with a given http status code exceeds a given threshold within a 15-minute window.
    *
    * @param threshold
    * Object with fields httpStatus, percentage, severity and httpMethod
    * @return
    * <pre> <pre>.withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS_404, 25.0)) # alert when 25% of all requests return status
    *  code 404 in a 15-minute window .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS_500, 25.0)) # alert when 25% of all
    *  requests return status code 500 in a 15-minute window .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS_504, 25.0,
    *  AlertSeverity.Warning, httpMethod.Post)) # raise a warning alert when 25% of all Post requests return status code 500 in a 15-minute window </pre>
    * </pre>
    */
  def withHttpStatusPercentThreshold(threshold: HttpStatusPercentThreshold): AlertConfigBuilder =
    this.copy(httpStatusPercentThresholds = httpStatusPercentThresholds :+ threshold)

  /** This will disable the ContainerKillThreshold alert which is configured with a threshold of 1 by default */
  def disableContainerKillThreshold(): AlertConfigBuilder =
    this.copy(containerKillThreshold = None)

  /** All microservices are deployed to MDTP inside docker containers. If the docker container runs out of memory then the container will be killed
    * with an out-of-memory exception. This alert will notify when a specified number of containers are killed within a 15-minute window.
    *
    * @param containerCrashThreshold
    *   The number of container kills to alert on
    * @return
    * <pre> <pre>.withContainerKillThreshold(5) # alert if 5 microservice instances are killed with an out-of-memory exception in a 15-minute
   *  window </pre> </pre>
    */
  def withContainerKillThreshold(containerCrashThreshold: Int): AlertConfigBuilder =
    this.copy(containerKillThreshold = Some(ContainerKillThreshold(containerCrashThreshold)))

  /** This alert will notify when your microservice receives a given number of requests within a 15-minute window.
    *
    * @param threshold
    *   The number of all http requests to alert on
    * @return
    * <pre> <pre>.withTotalHttpRequestThreshold(1000) # alert when 1000 requests are received in a 15-minute window </pre> </pre>
    */
  def withTotalHttpRequestThreshold(threshold: Int): AlertConfigBuilder =
    this.copy(totalHttpRequestThreshold = Some(TotalHttpRequestThreshold(threshold)))

  /** This alert will notify when the count of a given log message is logged exceeds a given threshold within a 15-minute window.
    * @param message
    *   The substring to search for in the log message
    * @param threshold
    *   The threshold above which an alert will be raised
    * @param lessThanMode
    *   Flips the logic so that an alert is raised if less than the threshold amount is detected
    * @param severity
    *   The severity to set for this check in PagerDuty
    * @return
    * <pre> <pre>.withLogMessageThreshold("Custom logs", 5) // occurrences of a specific log message in a 15-minute window
   *  .withLogMessageThreshold("heartbeat", 4, lessThanMode&#61;true) // triggers if a specific log message appears less than 4 times in a 15-minute
   *  window .withLogMessageThreshold("MY_LOG_MESSAGE", 10, severity &#61; AlertSeverity.Warning) // Raise warning alert in PagerDuty after 10 logs
   *  containing &#96;MY_LOG_MESSAGE&#96; are detected .withLogMessageThreshold("MY_LOG_MESSAGE", 2) // Raise an alert when a service generates two or more
   *  logs containing the specified MY_LOG_MESSAGE within a 15-minute timeframe. </pre>
    */
  def withLogMessageThreshold(message: String, threshold: Int, lessThanMode: Boolean = false, severity: AlertSeverity = AlertSeverity.Critical): AlertConfigBuilder =
    this.copy(logMessageThresholds = logMessageThresholds :+ LogMessageThreshold(message, threshold, lessThanMode, severity))

  /** This alert will notify when the average CPU used by all instances of your microservice exceeds a given threshold within a 5-minute window.
    *
    * @param averageCPUThreshold
    *   The average percentage CPU used by all instances of your microservice
    * @return
    * @example
    * <pre> <pre>.withAverageCPUThreshold(50) # alert if average CPU usage across all microservice instances is above 50% for the last 15 minutes
   *  </pre> </pre>
    */
  def withAverageCPUThreshold(averageCPUThreshold: Int): AlertConfigBuilder =
    this.copy(averageCPUThreshold = Some(AverageCPUThreshold(averageCPUThreshold)))

  /** In order to generate an alert for a service, alert-config expects an entry in app-config-\$ENV for that service. However, since platform
    * services don't have app-config entries by design, the creation of alerts for these services can be forced by adding .isPlatformService(true) to
    * your configBuilder.
    *
    * @deprecated
    *   Custom alerting coming soon!
    * @param platformService
    *   True if you are defining alerts for something that is NOT a standard microservice
    * @return
    */
  def isPlatformService(platformService: Boolean): AlertConfigBuilder =
    this.copy(platformService = platformService)

}

case class TeamAlertConfigBuilder(
    services: Seq[String],
    integrations: Seq[String] = Nil,
    errorsLoggedThreshold: Option[ErrorsLoggedThreshold] = None,
    exceptionThreshold: Option[ExceptionThreshold] = Some(ExceptionThreshold(2)),
    http5xxThreshold: Option[Http5xxThreshold] = None,
    http5xxPercentThreshold: Option[Http5xxPercentThreshold] = Some(Http5xxPercentThreshold(100.0)),
    http90PercentileResponseTimeThresholds: Seq[Http90PercentileResponseTimeThreshold] = Nil,
    httpAbsolutePercentSplitThresholds: Seq[HttpAbsolutePercentSplitThreshold] = Nil,
    httpAbsolutePercentSplitDownstreamServiceThresholds: Seq[HttpAbsolutePercentSplitDownstreamServiceThreshold] = Nil,
    httpAbsolutePercentSplitDownstreamHodThresholds: Seq[HttpAbsolutePercentSplitDownstreamHodThreshold] = Nil,
    containerKillThreshold: Option[ContainerKillThreshold] = Some(ContainerKillThreshold(1)),
    httpTrafficThresholds: Seq[HttpTrafficThreshold] = Nil,
    httpStatusThresholds: Seq[HttpStatusThreshold] = Nil,
    httpStatusPercentThresholds: Seq[HttpStatusPercentThreshold] = Nil,
    metricsThresholds: Seq[MetricsThreshold] = Nil,
    logMessageThresholds: Seq[LogMessageThreshold] = Nil,
    totalHttpRequestThreshold: Option[TotalHttpRequestThreshold] = None,
    averageCPUThreshold: Option[AverageCPUThreshold] = None,
    platformService: Boolean = false
) {

  /** Set of integrations for the configuration, e.g. .withIntegrations("dass-cyclone")
    * @param integrations
    *   List of names of your pagerduty integrations. These integration must already exist in PagerDuty; alert-config will not create them for you.
    */
  def withIntegrations(integrations: String*): TeamAlertConfigBuilder =
    this.copy(integrations = integrations)

  /** This alert will notify when your microservice logs a specified number of logs at ERROR log level within a 15-minute window.
    *
    * @param errorsLoggedThreshold
    *   The number of logs at ERROR level that this alert will trigger on
    * @return
    * @example
    * <pre> <pre>.withErrorsLoggedThreshold(5) # alert when 5 logs at ERROR level are logged in a 15-minute window </pre> </pre>
    */
  def withErrorsLoggedThreshold(errorsLoggedThreshold: Int): TeamAlertConfigBuilder =
    this.copy(errorsLoggedThreshold = Some(ErrorsLoggedThreshold(errorsLoggedThreshold)))

  /** This will disable the ExceptionThreshold alert which is configured with a threshold of 2 by default */
  def disableExceptionThreshold(): TeamAlertConfigBuilder = this.copy(exceptionThreshold = None)

  /** This alert will notify when your microservice throws a specified number of exceptions, at log level ERROR, within a 15-minute window.
    * @param exceptionThreshold
    *   The number of exceptions thrown that this alert will trigger on
    * @return
    * @example
    * <pre> <pre>.withExceptionThreshold(count: Int, severity: AlertSeverity &#61; AlertSeverity.Critical) </pre> </pre>
    */
  def withExceptionThreshold(exceptionThreshold: Int, severity: AlertSeverity = AlertSeverity.Critical): TeamAlertConfigBuilder =
    this.copy(exceptionThreshold = Some(ExceptionThreshold(exceptionThreshold, severity)))

  /** This alert will notify when the number of http responses returning a 5xx http status code exceeds a given threshold within a 15-minute window.
    *
    * @param http5xxThreshold
    *   The number of all http responses with a 5xx status code to alert on
    * @param severity
    *   Whether to raise the alert as critical or warning
    * @return
    * @example
    * <pre> <pre>.withHttp5xxThreshold(5) # alert when 5 requests return a 5xx status code in a 15-minute window .withHttp5xxThreshold(5,
   *  AlertSeverity.Warning) # raise a warning alert when 5 requests return a 5xx status code in a 15-minute window </pre> </pre>
    */
  def withHttp5xxThreshold(http5xxThreshold: Int, severity: AlertSeverity = AlertSeverity.Critical): TeamAlertConfigBuilder =
    this.copy(http5xxThreshold = Some(Http5xxThreshold(http5xxThreshold, severity)))

  /** This will disable the Http5xxPercentThreshold alert which is configured with a threshold of 100% by default */
  def disableHttp5xxPercentThreshold(): TeamAlertConfigBuilder = this.copy(http5xxPercentThreshold = None)

  /** This alert will notify when the percentage of http responses returning a 5xx http status code exceeds a given threshold within a 15-minute
    * window.
    *
    * This alert is enabled by default at 100%, but can be disabled by using .disableHttp5xxPercentThreshold().
    *
    * @param percentThreshold
    *   The %age of requests that must be 5xx to trigger the alarm
    * @param minimumHttp5xxCountThreshold
    *   The minimum count of 5xxs that must be present for the percentThreshold check to kick in. Optional. If you want to create a
    *   5xxPercentThreshold alert but only if you have a given count of 5xxs, this is the method to use. You want to use this parameter if, for
    *   example, you don't want to alert on just a one-off 5xx in the middle of the night Defaults to 0, which would mean the parameter has no effect.
    * @param severity
    *   How severe the alert is
    * @return
    *   Configured threshold object
    * @example
    * <pre> <pre>.withHttp5xxPercentThreshold(25.0) # alert when 25% of all requests return a 5xx status code in a 15-minute window
   *  .withHttp5xxPercentThreshold(25.0, AlertSeverity.Warning) # raise a warning alert when 25% of all requests return a 5xx status code in a
   *  15-minute window .withHttp5xxPercentThreshold(25.0, 5, AlertSeverity.Warning) # raise a warning alert when 25% of all requests return a 5xx
   *  status code in a 15-minute window, where there are at least 5 total 5xx status codes observed in the time window </pre> </pre>
    */
  def withHttp5xxPercentThreshold(percentThreshold: Double, minimumHttp5xxCountThreshold: Int = 0, severity: AlertSeverity = AlertSeverity.Critical): TeamAlertConfigBuilder =
    if (percentThreshold > 100) {
      println(Console.CYAN + s" = ${percentThreshold}" + Console.RESET)
      throw new Exception(
        s"withHttp5xxPercentThreshold percentThreshold '${percentThreshold}' cannot be over 100% - use .disableHttp5xxPercentThreshold() to disable this alert")
    } else {
      this.copy(http5xxPercentThreshold = Some(Http5xxPercentThreshold(percentThreshold, minimumHttp5xxCountThreshold, severity)))
    }

  /** This alert will notify you when the 90th Percentile request time goes above the defined thresholds for the time period specified by the user.
    * @param threshold
    *   Object representing the response time in milliseconds above which alerts will be raised at given severities for a given time period
    * @return
    * @example
    * <pre> <pre>.withHttp90PercentileResponseTimeThreshold(Http90PercentileResponseTimeThreshold(warning &#61; Some(1000), critical &#61; Some(2000),
   *  timePeriod &#61; 7)) </pre> </pre>
    */
  def withHttp90PercentileResponseTimeThreshold(threshold: Http90PercentileResponseTimeThreshold): TeamAlertConfigBuilder = {
    if (http90PercentileResponseTimeThresholds.nonEmpty) {
      throw new Exception("withHttp90PercentileResponseTimeThreshold has already been defined for this microservice")
    } else if (threshold.timePeriod < 0 || threshold.timePeriod > 15) {
      println(Console.CYAN + s" = ${threshold}" + Console.RESET)
      throw new Exception(
        s"withHttp90PercentileResponseTimeThreshold timePeriod '${threshold.timePeriod}' needs to be in the range 1-15 minutes (inclusive)")
    } else {
      this.copy(http90PercentileResponseTimeThresholds = Seq(threshold))
    }
  }

  /** @param threshold
    *   Object with fields percentThreshold, crossover, absoluteThreshold, hysteresis, excludeSpikes, errorFilter, severity
    * @return
    * @example
    * <pre> <pre>.withHttpAbsolutePercentSplitThreshold(HttpAbsolutePercentSplitThreshold(100.0, Int.MaxValue, 40, 1.0, 2, "status:499")) </pre> </pre>
    */
  def withHttpAbsolutePercentSplitThreshold(threshold: HttpAbsolutePercentSplitThreshold): TeamAlertConfigBuilder =
    this.copy(httpAbsolutePercentSplitThresholds = httpAbsolutePercentSplitThresholds :+ threshold)

  /** @param threshold
   * Object with fields percentThreshold, crossover, absoluteThreshold, hysteresis, excludeSpikes, errorFilter, target, severity
   * @return
   * @example
   * <pre> <pre>.withHttpAbsolutePercentSplitDownstreamServiceThreshold(HttpAbsolutePercentSplitDownstreamServiceThreshold(10.0, 0, -1, 1.1, 2,
   * "status:>498", "nps-hod-service",AlertSeverity.Critical)) </pre> </pre>
   */
  def withHttpAbsolutePercentSplitDownstreamServiceThreshold(threshold: HttpAbsolutePercentSplitDownstreamServiceThreshold): TeamAlertConfigBuilder =
    this.copy(httpAbsolutePercentSplitDownstreamServiceThresholds = httpAbsolutePercentSplitDownstreamServiceThresholds :+ threshold)

  /** @param threshold
   * Object with fields percentThreshold, crossover, absoluteThreshold, hysteresis, excludeSpikes, errorFilter, target, severity
   * @return
   * @example
   * <pre> <pre>.withHttpAbsolutePercentSplitDownstreamHodThreshold(HttpAbsolutePercentSplitDownstreamHodThreshold(10.0, 0, -1, 1.1, 2,
   * "status:>498", "nps-hod-service",AlertSeverity.Critical)) </pre> </pre>
   */
  def withHttpAbsolutePercentSplitDownstreamHodThreshold(threshold: HttpAbsolutePercentSplitDownstreamHodThreshold): TeamAlertConfigBuilder =
    this.copy(httpAbsolutePercentSplitDownstreamHodThresholds = httpAbsolutePercentSplitDownstreamHodThresholds :+ threshold)

  /** This will disable the ContainerKillThreshold alert which is configured with a threshold of 1 by default */
  def disableContainerKillThreshold(): TeamAlertConfigBuilder =
    this.copy(containerKillThreshold = None)

  /** All microservices are deployed to MDTP inside docker containers. If the docker container runs out of memory then the container will be killed
    * with an out-of-memory exception. This alert will notify when a specified number of containers are killed within a 15-minute window.
    *
    * @param containerKillThreshold
    *   The number of container kills to alert on
    * @return
    * <pre> <pre>.withContainerKillThreshold(5) # alert if 5 microservice instances are killed with an out-of-memory exception in a 15-minute
   *  window </pre> </pre>
    */
  def withContainerKillThreshold(containerKillThreshold: Int): TeamAlertConfigBuilder =
    this.copy(containerKillThreshold = Some(ContainerKillThreshold(containerKillThreshold)))

  /** This alert will notify you when the total number of requests received by the microservice is below a certain threshold.
    *
    * One or both of warning and critical must be given.
    *
    * @param threshold
    *   Object with fields warning, critical and maxMinutesBelowThreshold
    * @return
    * @example
    * <pre> <pre>.withHttpTrafficThreshold(HttpTrafficThreshold(10, 5, 15)) # alert with a warning when less than 10
    * requests or critical when less than 5 requests are received in a 15-minute
    * </pre>
    */
  def withHttpTrafficThreshold(threshold: HttpTrafficThreshold): TeamAlertConfigBuilder = {
    if (httpTrafficThresholds.nonEmpty) {
      throw new Exception("withHttpTrafficThreshold has already been defined for this microservice")
    } else {
      this.copy(httpTrafficThresholds = Seq(threshold))
    }
  }

  /** This alert will notify when your microservice returns a specified number of requests with a given http status code (between 400 and 599) within
    * a 15-minute window.
    * @param threshold
    *   Object with fields httpStatus, count, severity and httpMethod
    * @return
    *
    * @example
    * <pre> <pre>.withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_500, 5)) # alert when 5 occurences of status code 500 in a 15-minute
   *  window .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS(501), 5)) # alert when 5 occurences of status code 501 in a 15-minute window
   *  .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_502, 5, AlertSeverity.Warning)) # raise a warning alert when 5 occurences of status
   *  code 502 in a 15-minute window .withHttpStatusThreshold(HttpStatusThreshold(HTTP_STATUS_503, 5, AlertSeverity.Warning, httpMethod.Post)) #
   *  raise a warning alert when 5 occurences of Post requests with response status code 503 in a 15-minute window </pre> </pre>
    */
  def withHttpStatusThreshold(threshold: HttpStatusThreshold): TeamAlertConfigBuilder =
    this.copy(httpStatusThresholds = httpStatusThresholds :+ threshold)

  /** This alert will notify when the percentage of http responses with a given http status code exceeds a given threshold within a 15-minute window.
    *
    * @param threshold
    *   Object with fields httpStatus, percentage, severity and httpMethod
    * @return
    * <pre> <pre>.withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS_404, 25.0)) # alert when 25% of all requests return status
   *  code 404 in a 15-minute window .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS_500, 25.0)) # alert when 25% of all
   *  requests return status code 500 in a 15-minute window .withHttpStatusPercentThreshold(HttpStatusPercentThreshold(HTTP_STATUS_504, 25.0,
   *  AlertSeverity.Warning, httpMethod.Post)) # raise a warning alert when 25% of all Post requests return status code 500 in a 15-minute window </pre>
    * </pre>
    */
  def withHttpStatusPercentThreshold(threshold: HttpStatusPercentThreshold): TeamAlertConfigBuilder =
    this.copy(httpStatusPercentThresholds = httpStatusPercentThresholds :+ threshold)

  /** This alert will notify when your microservice receives a given number of requests within a 15-minute window.
    *
    * @param threshold
    *   The number of all http requests to alert on
    * @return
    * <pre> <pre>.withTotalHttpRequestThreshold(1000) # alert when 1000 requests are received in a 15-minute window </pre> </pre>
    */
  def withTotalHttpRequestThreshold(threshold: Int): TeamAlertConfigBuilder =
    this.copy(totalHttpRequestThreshold = Some(TotalHttpRequestThreshold(threshold)))

  /** This alert will notify when the count of a given log message is logged exceeds a given threshold within a 15-minute window.
    * @param message
    *   The substring to search for in the log message
    * @param threshold
    *   The threshold above which an alert will be raised
    * @param lessThanMode
    *   Flips the logic so that an alert is raised if less than the threshold amount is detected
    * @param severity
    *   The severity to set for this check in PagerDuty
    * @return
    * <pre> <pre>.withLogMessageThreshold("Custom logs", 5) // occurrences of a specific log message in a 15-minute window
   *  .withLogMessageThreshold("heartbeat", 4, lessThanMode&#61;true) // triggers if a specific log message appears less than 4 times in a 15-minute
   *  window .withLogMessageThreshold("MY_LOG_MESSAGE", 10, severity &#61; AlertSeverity.Warning) // Raise warning alert in PagerDuty after 10 logs
   *  containing &#96;MY_LOG_MESSAGE&#96; are detected .withLogMessageThreshold("MY_LOG_MESSAGE", 2) // Raise an alert when a service generates two or more
   *  logs containing the specified MY_LOG_MESSAGE within a 15-minute timeframe. </pre>
    */
  def withLogMessageThreshold(message: String, threshold: Int, lessThanMode: Boolean = false, severity: AlertSeverity = AlertSeverity.Critical): TeamAlertConfigBuilder =
    this.copy(logMessageThresholds = logMessageThresholds :+ LogMessageThreshold(message, threshold, lessThanMode, severity))

  /** This alert will notify when the average CPU used by all instances of your microservice exceeds a given threshold within a 5-minute window.
    *
    * @param averageCPUThreshold
    *   The average percentage CPU used by all instances of your microservice
    * @return
    * @example
    * <pre> <pre>.withAverageCPUThreshold(50) # alert if average CPU usage across all microservice instances is above 50% for the last 15 minutes
   *  </pre> </pre>
    */
  def withAverageCPUThreshold(averageCPUThreshold: Int): TeamAlertConfigBuilder =
    this.copy(averageCPUThreshold = Some(AverageCPUThreshold(averageCPUThreshold)))

  /** In order to generate an alert for a service, alert-config expects an entry in app-config-\$ENV for that service. However, since platform
    * services don't have app-config entries by design, the creation of alerts for these services can be forced by adding .isPlatformService(true) to
    * your configBuilder.
    *
    * @deprecated
    *   Custom alerting coming soon!
    * @param platformService
    *   True if you are defining alerts for something that is NOT a standard microservice
    * @return
    */
  def isPlatformService(platformService: Boolean): TeamAlertConfigBuilder =
    this.copy(platformService = platformService)

  def build(): Seq[AlertConfigBuilder] =
    services.map(service =>
      AlertConfigBuilder(
        service,
        integrations,
        errorsLoggedThreshold,
        exceptionThreshold,
        http5xxThreshold,
        http5xxPercentThreshold,
        http90PercentileResponseTimeThresholds,
        httpAbsolutePercentSplitThresholds,
        httpAbsolutePercentSplitDownstreamServiceThresholds,
        httpAbsolutePercentSplitDownstreamHodThresholds,
        containerKillThreshold,
        httpTrafficThresholds,
        httpStatusThresholds,
        httpStatusPercentThresholds,
        metricsThresholds,
        logMessageThresholds,
        totalHttpRequestThreshold,
        averageCPUThreshold,
        platformService
      ))

}

object TeamAlertConfigBuilder {

  def teamAlerts(services: Seq[String]): TeamAlertConfigBuilder = {
    require(services.nonEmpty, "no alert service provided")
    TeamAlertConfigBuilder(services)
  }

}
