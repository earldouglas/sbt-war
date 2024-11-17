package com.earldouglas

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class WebappComponentsRunnerTest
    extends AnyFunSuite
    with Matchers
    with BeforeAndAfterAll {

  lazy val configuration: WebappComponentsConfiguration =
    WebappComponentsConfiguration
      .load("src/test/resources/webapp-components.properties")

  lazy val runner: WebappComponentsRunner =
    new WebappComponentsRunner(configuration)

  override def beforeAll(): Unit = {
    runner.start.run()
  }

  override def afterAll(): Unit = {
    runner.stop.run()
  }

  test("GET /foo.html") {

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

  test("GET /bar.html") {

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

  test("GET /baz/raz.css") {

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

  test("GET /hello") {

    val expected: HttpClient.Response =
      HttpClient.Response(
        status = 200,
        headers = Map(
          "Content-Type" -> "text/plain"
        ),
        body = """|Hello, world!
                  |""".stripMargin
      )

    val obtained: HttpClient.Response =
      HttpClient.request(
        method = "GET",
        url = "http://localhost:8989/hello",
        headers = Map.empty,
        body = None
      )

    obtained.copy(
      headers = obtained.headers
        .filter { case (k, _) =>
          k == "Content-Type"
        }
        .map { case (k, v) =>
          (k, v.replaceAll(";charset=.*", ""))
        }
    ) shouldBe expected
  }

  test("GET /annotation") {

    val expected: HttpClient.Response =
      HttpClient.Response(
        status = 200,
        headers = Map(
          "Content-Type" -> "text/plain"
        ),
        body = """|Hello, annotation!
                  |""".stripMargin
      )

    val obtained: HttpClient.Response =
      HttpClient.request(
        method = "GET",
        url = "http://localhost:8989/annotation",
        headers = Map.empty,
        body = None
      )

    obtained.copy(
      headers = obtained.headers
        .filter { case (k, _) =>
          k == "Content-Type"
        }
        .map { case (k, v) =>
          (k, v.replaceAll(";charset=.*", ""))
        }
    ) shouldBe expected
  }
}
