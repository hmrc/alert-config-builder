val libName = "alert-config-builder"

lazy val library = Project(libName, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    majorVersion := 0,
    makePublicallyAvailableOnBintray := true,
    scalaVersion := "2.11.6",
    scalacOptions ++= Seq(
      "-Xlint",
      "-target:jvm-1.8",
      "-Xmax-classfile-name",
      "100",
      "-encoding",
      "UTF-8"
    )
  )
  .settings(
    parallelExecution in Test := false,
    fork in Test := false
  )
  .settings(
    libraryDependencies ++= LibDependencies.compile ++ LibDependencies.test)
