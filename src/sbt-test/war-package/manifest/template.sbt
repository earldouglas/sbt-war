TaskKey[Unit]("setup") := {

  sbt.io.IO.copyDirectory(
    new java.io.File(sys.props("templateDirectory")),
    baseDirectory.value
  )

  sbt.io.IO.copyFile(
    new java.io.File("sbt", "test.sbt"),
    new java.io.File("test.sbt")
  )

  sbtVersion.value match {
    case x if x.startsWith("1.") =>
      sbt.io.IO.append(
        new java.io.File("test.sbt"),
        sbt.io.IO.read(
          new java.io.File("sbt", "test-1.x.sbt")
        )
      )
    case x if x.startsWith("2.") =>
      sbt.io.IO.append(
        new java.io.File("test.sbt"),
        sbt.io.IO.read(
          new java.io.File("sbt", "test-2.x.sbt")
        )
      )
  }

}
