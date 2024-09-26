package com.earldouglas.sbt.war

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.io.File

class WebappComponentsTest extends AnyFunSuite with Matchers {

  val fakeproject: File =
    new File("src/test/fakeproject/src/main/webapp")

  val fakeClasspath: Seq[File] =
    Seq(
      "src/test/fakeproject/classes",
      "src/test/fakeproject/lib/baz.jar",
      "src/test/fakeproject/lib/raz.jar"
    ).map(new File(_))

  test("getResources") {

    val expected: Map[String, File] =
      Map(
        "bar.html" -> "src/test/fakeproject/src/main/webapp/bar.html",
        "baz/raz.css" -> "src/test/fakeproject/src/main/webapp/baz/raz.css",
        "foo.html" -> "src/test/fakeproject/src/main/webapp/foo.html"
      ).map { case (path, file) => path -> new File(file) }

    val obtained: Map[String, File] =
      WebappComponents.getResources(fakeproject)

    obtained shouldBe expected
  }

  test("getClasses") {

    val expected: Map[String, File] =
      Map(
        "WEB-INF/classes/bar.class" -> "src/test/fakeproject/classes/bar.class",
        "WEB-INF/classes/foo.class" -> "src/test/fakeproject/classes/foo.class"
      ).map { case (path, file) => path -> new File(file) }

    val obtained: Map[String, File] =
      WebappComponents.getClasses(fakeClasspath)

    obtained shouldBe expected
  }

  test("getLib") {

    val expected: Map[String, File] =
      Map(
        "WEB-INF/lib/baz.jar" -> "src/test/fakeproject/lib/baz.jar",
        "WEB-INF/lib/raz.jar" -> "src/test/fakeproject/lib/raz.jar"
      ).map { case (path, file) => path -> new File(file) }

    val obtained: Map[String, File] =
      WebappComponents.getLib(fakeClasspath)

    obtained shouldBe expected
  }
}
