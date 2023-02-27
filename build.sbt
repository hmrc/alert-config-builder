val libName = "alert-config-builder"

lazy val library = Project(libName, file("."))
  .settings(
    majorVersion := 0,
    isPublicArtefact := true,
    scalaVersion := "2.11.6",
  )
  .settings(libraryDependencies ++= LibDependencies.compile ++ LibDependencies.test)
