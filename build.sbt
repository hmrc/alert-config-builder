lazy val library = Project("alert-config-builder", file("."))
  .settings(
    majorVersion := 0,
    isPublicArtefact := true,
    scalaVersion := "2.13.8",
  )
  .settings(libraryDependencies ++= LibDependencies.compile ++ LibDependencies.test)
