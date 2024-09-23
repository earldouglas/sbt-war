enablePlugins(WarPackagePlugin)

TaskKey[Unit]("check") := {
  checkWar.value
}

lazy val checkWar: Def.Initialize[Task[Unit]] =
  Def.task {

    val log: sbt.internal.util.ManagedLogger = streams.value.log

    import Keys.{`package` => pkg}
    import collection.JavaConverters._
    import java.util.zip.ZipEntry
    import java.util.zip.ZipFile
    import java.util.{Enumeration => JEnumeration}

    def assertEquals(
        name: String,
        expected: Set[String],
        obtained: Set[String]
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

    val expected: Set[String] =
      Set(
        "META-INF/MANIFEST.MF",
        "WEB-INF/",
        "WEB-INF/web.xml",
        "classes/",
        "classes/domain/",
        "classes/domain/entities/",
        "classes/domain/entities/Count$.class",
        "classes/domain/entities/Count.class",
        "classes/domain/entities/Count.tasty",
        "classes/domain/operations/",
        "classes/domain/operations/Counter.class",
        "classes/domain/operations/Counter.tasty",
        "classes/drivers/",
        "classes/drivers/db/",
        "classes/drivers/db/Transactor.class",
        "classes/drivers/db/Transactor.tasty",
        "classes/drivers/db/db$package$$anon$1$$anon$2.class",
        "classes/drivers/db/db$package$$anon$1.class",
        "classes/drivers/db/db$package$.class",
        "classes/drivers/db/db$package.class",
        "classes/drivers/db/db$package.tasty",
        "classes/drivers/mem/",
        "classes/drivers/mem/mem$package$$anon$1$$anon$2.class",
        "classes/drivers/mem/mem$package$$anon$1.class",
        "classes/drivers/mem/mem$package$.class",
        "classes/drivers/mem/mem$package.class",
        "classes/drivers/mem/mem$package.tasty",
        "classes/runners/",
        "classes/runners/Main$$anon$1.class",
        "classes/runners/Main$tx$.class",
        "classes/runners/Main.class",
        "classes/runners/Main.tasty",
        "classes/runners/MainServlet.class",
        "classes/runners/MainServlet.tasty",
        "classes/usecases/",
        "classes/usecases/Counter.class",
        "classes/usecases/Counter.tasty",
        "favicon.ico",
        "index.html",
        "lib/",
        "lib/cats-core_3-2.9.0.jar",
        "lib/cats-effect-kernel_3-3.5.4.jar",
        "lib/cats-effect-std_3-3.5.4.jar",
        "lib/cats-effect_3-3.5.4.jar",
        "lib/cats-kernel_3-2.9.0.jar",
        "lib/h2-2.2.224.jar",
        "lib/scala-library-2.13.14.jar",
        "lib/scala3-library_3-3.5.0.jar",
        "styles/",
        "styles/theme.css"
      )

    val warFile: File = pkg.value
    val zipFile: ZipFile = new ZipFile(warFile)
    val zipEntries: JEnumeration[_ <: ZipEntry] = zipFile.entries()
    val contents: Set[String] = zipEntries.asScala.map(_.getName()).toSet

    assertEquals(
      name = "WarPackagePlugin: checkWar",
      expected = expected,
      obtained = contents
    )
  }
