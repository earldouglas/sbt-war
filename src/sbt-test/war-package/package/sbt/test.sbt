enablePlugins(WarPackagePlugin)

////////////////////////////////////////////////////////////////////////

TaskKey[Unit]("check-no-export-jars") := {

  val log: sbt.internal.util.ManagedLogger = streams.value.log

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
      "WEB-INF/lib/scala-library-2.13.16.jar",
      "WEB-INF/lib/scala-logging_3-3.9.5.jar",
      "WEB-INF/lib/scala3-library_3-3.7.3.jar",
      "WEB-INF/lib/slf4j-api-2.0.15.jar",
      "WEB-INF/web.xml",
      "favicon.ico",
      "index.html",
      "styles/",
      "styles/theme.css"
    )

  val zipFile: ZipFile = new ZipFile(warFile.value)
  val zipEntries: JEnumeration[_ <: ZipEntry] = zipFile.entries()
  val contents: Set[String] =
    zipEntries.asScala.map(_.getName()).toSet

  assertEquals(
    name = "WarPackagePlugin: package (exportJars := false)",
    expected = expected,
    obtained = contents
  )
}

TaskKey[Unit]("check-export-jars") := {

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
      "WEB-INF/lib/",
      "WEB-INF/lib/cats-core_3-2.9.0.jar",
      "WEB-INF/lib/cats-effect-kernel_3-3.5.4.jar",
      "WEB-INF/lib/cats-effect-std_3-3.5.4.jar",
      "WEB-INF/lib/cats-effect_3-3.5.4.jar",
      "WEB-INF/lib/cats-kernel_3-2.9.0.jar",
      "WEB-INF/lib/h2-2.2.224.jar",
      "WEB-INF/lib/logback-classic-1.5.8.jar",
      "WEB-INF/lib/logback-core-1.5.8.jar",
      "WEB-INF/lib/scala-library-2.13.16.jar",
      "WEB-INF/lib/scala-logging_3-3.9.5.jar",
      "WEB-INF/lib/scala3-library_3-3.7.3.jar",
      "WEB-INF/lib/slf4j-api-2.0.15.jar",
      "WEB-INF/lib/test_3-0.1.0-SNAPSHOT.jar",
      "WEB-INF/web.xml",
      "favicon.ico",
      "index.html",
      "styles/",
      "styles/theme.css"
    )

  val zipFile: ZipFile = new ZipFile(warFile.value)
  val zipEntries: JEnumeration[_ <: ZipEntry] = zipFile.entries()
  val contents: Set[String] =
    zipEntries.asScala.map(_.getName()).toSet

  assertEquals(
    name = "WarPackagePlugin: package (exportJars := true)",
    expected = expected,
    obtained = contents
  )
}
