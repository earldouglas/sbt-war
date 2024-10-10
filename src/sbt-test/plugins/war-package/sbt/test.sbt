enablePlugins(WarPackagePlugin)

TaskKey[Unit]("check") := {
  checkWar.value
}

lazy val checkWar: Def.Initialize[Task[Unit]] =
  Def.task {

    val log: sbt.internal.util.ManagedLogger = streams.value.log

    import sbt.Keys.{`package` => pkg}
    import scala.collection.JavaConverters._
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
        "WEB-INF/classes/",
        "WEB-INF/classes/domain/",
        "WEB-INF/classes/domain/entities/",
        "WEB-INF/classes/domain/entities/Count$.class",
        "WEB-INF/classes/domain/entities/Count.class",
        "WEB-INF/classes/domain/entities/Count.tasty",
        "WEB-INF/classes/domain/operations/",
        "WEB-INF/classes/domain/operations/Counter.class",
        "WEB-INF/classes/domain/operations/Counter.tasty",
        "WEB-INF/classes/drivers/",
        "WEB-INF/classes/drivers/db/",
        "WEB-INF/classes/drivers/db/Transactor.class",
        "WEB-INF/classes/drivers/db/Transactor.tasty",
        "WEB-INF/classes/drivers/db/db$package$$anon$1$$anon$2.class",
        "WEB-INF/classes/drivers/db/db$package$$anon$1.class",
        "WEB-INF/classes/drivers/db/db$package$.class",
        "WEB-INF/classes/drivers/db/db$package.class",
        "WEB-INF/classes/drivers/db/db$package.tasty",
        "WEB-INF/classes/drivers/mem/",
        "WEB-INF/classes/drivers/mem/mem$package$$anon$1$$anon$2.class",
        "WEB-INF/classes/drivers/mem/mem$package$$anon$1.class",
        "WEB-INF/classes/drivers/mem/mem$package$.class",
        "WEB-INF/classes/drivers/mem/mem$package.class",
        "WEB-INF/classes/drivers/mem/mem$package.tasty",
        "WEB-INF/classes/logback.xml",
        "WEB-INF/classes/runners/",
        "WEB-INF/classes/runners/CountServlet.class",
        "WEB-INF/classes/runners/CountServlet.tasty",
        "WEB-INF/classes/runners/HelloServlet.class",
        "WEB-INF/classes/runners/HelloServlet.tasty",
        "WEB-INF/classes/runners/Main$$anon$1.class",
        "WEB-INF/classes/runners/Main$tx$.class",
        "WEB-INF/classes/runners/Main.class",
        "WEB-INF/classes/runners/Main.tasty",
        "WEB-INF/classes/usecases/",
        "WEB-INF/classes/usecases/Counter.class",
        "WEB-INF/classes/usecases/Counter.tasty",
        "WEB-INF/lib/",
        "WEB-INF/lib/cats-core_3-2.9.0.jar",
        "WEB-INF/lib/cats-effect-kernel_3-3.5.4.jar",
        "WEB-INF/lib/cats-effect-std_3-3.5.4.jar",
        "WEB-INF/lib/cats-effect_3-3.5.4.jar",
        "WEB-INF/lib/cats-kernel_3-2.9.0.jar",
        "WEB-INF/lib/h2-2.2.224.jar",
        "WEB-INF/lib/logback-classic-1.5.8.jar",
        "WEB-INF/lib/logback-core-1.5.8.jar",
        "WEB-INF/lib/scala-library-2.13.14.jar",
        "WEB-INF/lib/scala-logging_3-3.9.5.jar",
        "WEB-INF/lib/scala3-library_3-3.5.0.jar",
        "WEB-INF/lib/slf4j-api-2.0.15.jar",
        "WEB-INF/web.xml",
        "favicon.ico",
        "index.html",
        "styles/",
        "styles/theme.css",
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

InputKey[Unit]("checkManifest") := {

  import java.io.InputStream
  import java.util.zip.ZipFile
  import scala.io.Source

  val args: Seq[String] = Def.spaceDelimited("<arg>").parsed

  val zipFile: ZipFile = new ZipFile(args(0))
  val rule: String = args(1)
  val manifestAttribute: String = args(2)

  val manifestFilename: String = "META-INF/MANIFEST.MF"

  Option(zipFile.getEntry(manifestFilename)) match {
    case Some(e) =>
      val is: InputStream = zipFile.getInputStream(e)
      val manifestLines: Seq[String] =
        Source.fromInputStream(is).getLines().toSeq

      (rule, manifestLines.contains(manifestAttribute)) match {
        case ("includes", true) =>
          ()
        case ("includes", false) =>
          sys.error(
            "Manifest " +
            manifestFilename +
            " is missing expected attribute " +
            manifestAttribute
          )
        case ("excludes", true) =>
          sys.error(
            "Manifest " +
            manifestFilename +
            " contains unexpected attribute " +
            manifestAttribute
          )
        case ("excludes", false) =>
          ()
        case _ =>
          sys.error(
            "Invalid rule " +
            rule
          )
      }
    case None =>
      sys.error(
        "File " +
        manifestFilename +
        " not found in zip " +
        zipFile
      )
  }
}
