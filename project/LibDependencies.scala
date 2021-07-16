import sbt._

object LibDependencies {

  val compile = Seq(
    "org.scala-lang" % "scala-reflect" % "2.12.14",
    "org.reflections" % "reflections" % "0.9.12",
    "io.spray" %% "spray-json" % "1.3.6",
    "org.yaml" % "snakeyaml" % "1.29"
  )


  val test = Seq(
    "org.json4s" %% "json4s-jackson" % "4.0.1" % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % "test",
    "org.pegdown" % "pegdown" % "1.6.0" % "test"
  )

}
