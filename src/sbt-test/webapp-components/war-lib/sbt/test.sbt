enablePlugins(WebappComponentsPlugin)

////////////////////////////////////////////////////////////////////////

lazy val scalaLibraryDotJar: Def.Initialize[String] =
  Def.setting {
    CrossVersion.partialVersion(sbtVersion.value) match {
      case Some((1, 13)) =>
        s"scala-library-${scalaVersion.value}.jar"
      case Some((2, 0)) =>
        s"scala-library-${scalaVersion.value}.jar"
      case Some((2, 1)) =>
        "scala-library.jar"
      case v =>
        throw new Exception(s"Unsupported sbt version ${v}")
    }
  }

lazy val dependencyJars: Def.Initialize[Set[String]] =
  Def.setting {
    Set(
      "cats-core_3-2.9.0.jar",
      "cats-effect-kernel_3-3.5.4.jar",
      "cats-effect-std_3-3.5.4.jar",
      "cats-effect_3-3.5.4.jar",
      "cats-kernel_3-2.9.0.jar",
      "h2-2.2.224.jar",
      "logback-classic-1.5.8.jar",
      "logback-core-1.5.8.jar",
      scalaLibraryDotJar.value,
      "scala-logging_3-3.9.5.jar",
      s"scala3-library_3-${scalaVersion.value}.jar",
      "slf4j-api-2.0.15.jar"
    )
  }

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
            |  scala version: ${scalaVersion.value}
            |  sbt version: ${sbtVersion.value}
            |  sizes match: ${!sizesDoNotMatch}
            |  mappings match: ${!mappingsDoNotMatch}
            |  missing:
            |${(expected -- obtained.keys.toSet).mkString("    - ", "\n    - ", "")}
            |  unexpected:
            |${(obtained.keys.toSet -- expected).mkString("    - ", "\n    - ", "")}
            |""".stripMargin
      )
    } else {
      log.success(name)
    }

  }

  val expected: Set[String] = dependencyJars.value

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
            |  scala version: ${scalaVersion.value}
            |  sbt version: ${sbtVersion.value}
            |  sizes match: ${!sizesDoNotMatch}
            |  mappings match: ${!mappingsDoNotMatch}
            |  missing:
            |${(expected -- obtained.keys.toSet).mkString("    - ", "\n    - ", "")}
            |  unexpected:
            |${(obtained.keys.toSet -- expected).mkString("    - ", "\n    - ", "")}
            |""".stripMargin
      )
    } else {
      log.success(name)
    }

  }

  val expected: Set[String] = dependencyJars.value + "test_3-0.1.0-SNAPSHOT.jar"

  assertContains(
    name = "WebappComponentsPlugin: warLib (exportJars := true)",
    expected = expected.map(x => s"WEB-INF/lib/${x}"),
    obtained = (Runtime / warLib).value
  )
}
