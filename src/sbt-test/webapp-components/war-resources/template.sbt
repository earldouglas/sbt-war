@transient
lazy val setup = taskKey[Unit]("Copy the shared scripted fixture.")

setup := {
  sbt.io.IO.copyDirectory(
    new java.io.File(sys.props("templateDirectory")),
    baseDirectory.value
  )
  sbt.io.IO.copyFile(
    new java.io.File("sbt", "test.sbt"),
    new java.io.File("test.sbt")
  )
}
