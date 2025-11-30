enablePlugins(WebappComponentsPlugin)

////////////////////////////////////////////////////////////////////////

TaskKey[Unit]("check-no-export-jars") := {

  val log: sbt.internal.util.ManagedLogger = streams.value.log

  def assertEquals(
      name: String,
      expected: Map[String, File],
      obtained: Map[String, File]
  ): Unit = {

    val sizesDoNotMatch = expected.size != obtained.size
    val mappingsDoNotMatch = expected != obtained

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

  val expected: Map[String, File] = {

    val expectedClasses: Map[String, File] =
      List(
        "domain/entities/Count$.class",
        "domain/entities/Count.class",
        "domain/entities/Count.tasty",
        "domain/operations/Counter.class",
        "domain/operations/Counter.tasty",
        "drivers/db/Transactor.class",
        "drivers/db/Transactor.tasty",
        "drivers/db/db$package$$anon$1$$anon$2.class",
        "drivers/db/db$package$$anon$1.class",
        "drivers/db/db$package$.class",
        "drivers/db/db$package.class",
        "drivers/db/db$package.tasty",
        "drivers/mem/mem$package$$anon$1$$anon$2.class",
        "drivers/mem/mem$package$$anon$1.class",
        "drivers/mem/mem$package$.class",
        "drivers/mem/mem$package.class",
        "drivers/mem/mem$package.tasty",
        "runners/CountServlet.class",
        "runners/CountServlet.tasty",
        "runners/HelloServlet.class",
        "runners/HelloServlet.tasty",
        "runners/Main$$anon$1.class",
        "runners/Main$tx$.class",
        "runners/Main.class",
        "runners/Main.tasty",
        "usecases/Counter.class",
        "usecases/Counter.tasty"
      )
        .map({ x =>
          val root: File = (Compile / classDirectory).value
          s"WEB-INF/classes/${x}" -> root / x
        })
        .toMap

    val expectedResources: Map[String, File] = {
      List(
        "logback.xml"
      )
        .map({ x =>
          val root: File =
            CrossVersion.partialVersion(sbtVersion.value) match {
              case Some((1, _)) =>
                (Compile / classDirectory).value
              case Some((2, _)) =>
                (Compile / resourceDirectory).value
              case v =>
                throw new Exception(s"Unsupported sbt version: ${v}")
            }
          s"WEB-INF/classes/${x}" -> root / x
        })
        .toMap
    }

    expectedClasses ++ expectedResources
  }

  assertEquals(
    name = "WebappComponentsPlugin: warClasses (exportJars := false)",
    expected = expected,
    obtained = (Runtime / warClasses).value
  )
}

TaskKey[Unit]("check-export-jars") := {

  val log: sbt.internal.util.ManagedLogger = streams.value.log

  def assertEquals(
      name: String,
      expected: Map[String, File],
      obtained: Map[String, File]
  ): Unit = {

    val sizesDoNotMatch = expected.size != obtained.size
    val mappingsDoNotMatch = expected != obtained

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

  assertEquals(
    name = "WebappComponentsPlugin: warClasses (exportJars := true)",
    expected = Map.empty[String, File],
    obtained = (Runtime / warClasses).value
  )
}
