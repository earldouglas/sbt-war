package com.earldouglas.sbt.war

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.FileAlreadyExistsException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

class WebappComponentsRunnerTest
    extends AnyFunSuite
    with Matchers
    with BeforeAndAfterAll {

  lazy val emptyDir: File = {
    val path: Path =
      FileSystems
        .getDefault()
        .getPath("target", "empty")
    try {
      Files.createDirectory(path)
      path.toFile()
    } catch {
      case e: FileAlreadyExistsException =>
        path.toFile()
    }
  }

  lazy val runner = {

    val webappPath: String =
      "src/test/fakeproject/src/main/webapp"

    WebappComponentsRunner(
      hostname = "localhost",
      port = 8989,
      contextPath = "",
      emptyWebappDir = emptyDir,
      emptyClassesDir = emptyDir,
      resourceMap = Map(
        "bar.html" -> new File(s"${webappPath}/bar.html"),
        "foo.html" -> new File(s"${webappPath}/foo.html"),
        "baz/raz.css" -> new File(s"${webappPath}/baz/raz.css")
      )
    )
  }

  override def beforeAll(): Unit = {
    runner.start()
  }

  override def afterAll(): Unit = {
    runner.stop()
  }

  test("/foo.html") {

    val expected: HttpClient.Response =
      HttpClient.Response(
        status = 200,
        headers = Map(
          "Content-Type" -> "text/html"
        ),
        body = """|<blink>foo</blink>
                  |""".stripMargin
      )

    val obtained: HttpClient.Response =
      HttpClient.request(
        method = "GET",
        url = "http://localhost:8989/foo.html",
        headers = Map.empty,
        body = None
      )

    obtained.copy(
      headers = obtained.headers.filter { case (k, _) =>
        k == "Content-Type"
      }
    ) shouldBe expected
  }

  test("/bar.html") {

    val expected: HttpClient.Response =
      HttpClient.Response(
        status = 200,
        headers = Map(
          "Content-Type" -> "text/html"
        ),
        body = """|<marquee>bar</marquee>
                  |""".stripMargin
      )

    val obtained: HttpClient.Response =
      HttpClient.request(
        method = "GET",
        url = "http://localhost:8989/bar.html",
        headers = Map.empty,
        body = None
      )

    obtained.copy(
      headers = obtained.headers.filter { case (k, _) =>
        k == "Content-Type"
      }
    ) shouldBe expected
  }

  test("/baz/raz.css") {

    val expected: HttpClient.Response =
      HttpClient.Response(
        status = 200,
        headers = Map(
          "Content-Type" -> "text/css"
        ),
        body = """|div.raz { font-weight: bold; }
                  |""".stripMargin
      )

    val obtained: HttpClient.Response =
      HttpClient.request(
        method = "GET",
        url = "http://localhost:8989/baz/raz.css",
        headers = Map.empty,
        body = None
      )

    obtained.copy(
      headers = obtained.headers.filter { case (k, _) =>
        k == "Content-Type"
      }
    ) shouldBe expected
  }
}
