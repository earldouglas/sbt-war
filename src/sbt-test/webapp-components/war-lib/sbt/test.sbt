enablePlugins(WebappComponentsPlugin)

////////////////////////////////////////////////////////////////////////

TaskKey[Unit]("check-no-export-jars") := {

  val log: sbt.internal.util.ManagedLogger = streams.value.log

  def assertContains(
      name: String,
      expected: Set[String],
      obtained: Map[String, File]
  ): Unit = {

    val sizesDoNotMatch = expected.size != obtained.size
    val mappingsDoNotMatch = expected != obtained.keys.toSet

    if (sizesDoNotMatch || mappingsDoNotMatch) {
      log.error(name)
      sys.error(
        s"""|${name}:
            |  expected:
            |${expected.mkString("    - ", "\n    - ", "")}
            |  obtained:
            |${obtained.mkString("    - ", "\n    - ", "")}
            |""".stripMargin
      )
    } else {
      log.success(name)
    }

  }

  val expected: Set[String] =
    Set(
      "cats-core_3-2.9.0.jar",
      "cats-effect-kernel_3-3.5.4.jar",
      "cats-effect-std_3-3.5.4.jar",
      "cats-effect_3-3.5.4.jar",
      "cats-kernel_3-2.9.0.jar",
      "h2-2.2.224.jar",
      "logback-classic-1.5.8.jar",
      "logback-core-1.5.8.jar",
      "scala-library-2.13.14.jar",
      "scala-logging_3-3.9.5.jar",
      "scala3-library_3-3.5.0.jar",
      "slf4j-api-2.0.15.jar"
    )

  assertContains(
    name = "WebappComponentsPlugin: warLib (exportJars := false)",
    expected = expected.map(x => s"WEB-INF/lib/${x}"),
    obtained = (Runtime / warLib).value
  )
}

TaskKey[Unit]("check-export-jars") := {

  val log: sbt.internal.util.ManagedLogger = streams.value.log

  def assertContains(
      name: String,
      expected: Set[String],
      obtained: Map[String, File]
  ): Unit = {

    val sizesDoNotMatch = expected.size != obtained.size
    val mappingsDoNotMatch = expected != obtained.keys.toSet

    if (sizesDoNotMatch || mappingsDoNotMatch) {
      log.error(name)
      sys.error(
        s"""|${name}:
            |  expected:
            |${expected.mkString("    - ", "\n    - ", "")}
            |  obtained:
            |${obtained.mkString("    - ", "\n    - ", "")}
            |""".stripMargin
      )
    } else {
      log.success(name)
    }

  }

  val expected: Set[String] =
    Set(
      "cats-core_3-2.9.0.jar",
      "cats-effect-kernel_3-3.5.4.jar",
      "cats-effect-std_3-3.5.4.jar",
      "cats-effect_3-3.5.4.jar",
      "cats-kernel_3-2.9.0.jar",
      "h2-2.2.224.jar",
      "logback-classic-1.5.8.jar",
      "logback-core-1.5.8.jar",
      "scala-library-2.13.14.jar",
      "scala-logging_3-3.9.5.jar",
      "scala3-library_3-3.5.0.jar",
      "slf4j-api-2.0.15.jar",
      "test_3-0.1.0-SNAPSHOT.jar",
    )

  assertContains(
    name = "WebappComponentsPlugin: warLib (exportJars := true)",
    expected = expected.map(x => s"WEB-INF/lib/${x}"),
    obtained = (Runtime / warLib).value
  )
}
