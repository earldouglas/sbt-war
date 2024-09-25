package com.earldouglas.sbt.war

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.io.File

class WarPackageTest extends AnyFunSuite with Matchers {

  val fakeproject: File =
    new File("src/test/fakeproject/src/main/webapp")

  val fakeClasspath: Seq[File] =
    Seq(
      "src/test/fakeproject/classes",
      "src/test/fakeproject/lib/baz.jar",
      "src/test/fakeproject/lib/raz.jar"
    ).map(new File(_))

  test("getWarContents") {

    val expected: Seq[(File, String)] =
      Seq(
        "src/test/fakeproject/src/main/webapp/foo.html" -> "foo.html",
        "src/test/fakeproject/src/main/webapp/bar.html" -> "bar.html",
        "src/test/fakeproject/src/main/webapp/baz/raz.css" -> "baz/raz.css",
        "src/test/fakeproject/classes/foo.class" -> "WEB-INF/classes/foo.class",
        "src/test/fakeproject/classes/bar.class" -> "WEB-INF/classes/bar.class",
        "src/test/fakeproject/lib/baz.jar" -> "WEB-INF/lib/baz.jar",
        "src/test/fakeproject/lib/raz.jar" -> "WEB-INF/lib/raz.jar"
      ).map { case (src, dst) => new File(src) -> dst }

    val obtained: Seq[(File, String)] =
      WarPackage.getWarContents(
        webappResources = WebappComponents.getResources(fakeproject),
        webappClasses = WebappComponents.getClasses(fakeClasspath),
        webappLib = WebappComponents.getLib(fakeClasspath)
      )

    obtained.sorted shouldBe expected.sorted
  }
}
