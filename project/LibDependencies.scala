import sbt._

object LibDependencies {

  val compile = Seq(
    "org.reflections" %  "reflections"   % "0.10.2",
    "io.spray"        %% "spray-json"    % "1.3.6",
    "org.yaml"        %  "snakeyaml"     % "1.33",
    "com.fasterxml.jackson.dataformat" %  "jackson-dataformat-yaml" % "2.15.3",
    "com.fasterxml.jackson.module"     %% "jackson-module-scala"    % "2.15.3"
  )


  val test = Seq(
    "org.json4s"           %% "json4s-jackson" % "4.0.6"    % Test,
    "org.scalatest"        %% "scalatest"      % "3.2.3"    % Test,
    "com.vladsch.flexmark" %  "flexmark-all"   % "0.35.10"  % Test
  )
}
