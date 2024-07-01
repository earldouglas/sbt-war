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

    val expected: Map[File, String] =
      Map(
        "src/test/fakeproject/src/main/webapp/foo.html" -> "foo.html",
        "src/test/fakeproject/src/main/webapp/bar.html" -> "bar.html",
        "src/test/fakeproject/src/main/webapp/baz/raz.css" -> "baz/raz.css"
      ).map { case (src, dst) => new File(src) -> dst }

    val obtained: Map[File, String] =
      WebappComponents.getResources(fakeproject)

    obtained shouldBe expected
  }

  test("getClasses") {

    val expected: Map[File, String] =
      Map(
        "src/test/fakeproject/classes/foo.class" -> "foo.class",
        "src/test/fakeproject/classes/bar.class" -> "bar.class"
      ).map { case (src, dst) => new File(src) -> dst }

    val obtained: Map[File, String] =
      WebappComponents.getClasses(fakeClasspath)

    obtained shouldBe expected
  }

  test("getLib") {

    val expected: Map[File, String] =
      Map(
        "src/test/fakeproject/lib/baz.jar" -> "baz.jar",
        "src/test/fakeproject/lib/raz.jar" -> "raz.jar"
      ).map { case (src, dst) => new File(src) -> dst }

    val obtained: Map[File, String] =
      WebappComponents.getLib(fakeClasspath)

    obtained shouldBe expected
  }
}
