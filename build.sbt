lazy val library = Project("alert-config-builder", file("."))
  .settings(
    majorVersion := 1,
    isPublicArtefact := true,
    scalaVersion := "2.13.12",
  )
  .settings(libraryDependencies ++= LibDependencies.compile ++ LibDependencies.test)
