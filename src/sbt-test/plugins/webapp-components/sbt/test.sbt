enablePlugins(WebappComponentsPlugin)

TaskKey[Unit]("check") := {
  checkResources.value
  checkClasses.value
  checkLib.value
}

val checkClasses: Def.Initialize[Task[Unit]] =
  Def.task {

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

    val expected: List[String] =
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
        "logback.xml",
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

    assertEquals(
      name = "WebappComponentsPlugin: checkClasses",
      expected = {
        val root: File = (Compile / classDirectory).value
        expected
          .map(x => s"WEB-INF/classes/${x}" -> root / x)
          .toMap
      },
      obtained = (Runtime / warClasses).value
    )
  }

val checkLib: Def.Initialize[Task[Unit]] =
  Def.task {

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
      name = "WebappComponentsPlugin: checkLib",
      expected = expected.map(x => s"WEB-INF/lib/${x}"),
      obtained = (Runtime / warLib).value
    )
  }

lazy val checkResources: Def.Initialize[Task[Unit]] =
  Def.task {

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
          s"""|${name}
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

    val expected: List[String] =
      List(
        "WEB-INF/web.xml",
        "favicon.ico",
        "index.html",
        "styles/theme.css"
      )

    assertEquals(
      name = "WebappComponentsPlugin: checkResources",
      expected = {
        val root: File = (Compile / sourceDirectory).value
        expected
          .map(x => x -> root / "webapp" / x)
          .toMap
      },
      obtained = (Runtime / warResources).value
    )
  }
