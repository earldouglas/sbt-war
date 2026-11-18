package com.earldouglas

import munit.FunSuite

class WarRunnerTest extends FunSuite {

  lazy val configuration: WarConfiguration =
    WarConfiguration.load("runner/src/test/resources/war.properties")

  lazy val runner: WarRunner = new WarRunner(configuration)

  override def beforeAll(): Unit = runner.start.run()

  override def afterAll(): Unit = runner.stop.run()

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
        url = "http://localhost:8080/foo.html",
        headers = Map.empty,
        body = None
      )

    assertEquals(
      obtained = obtained.copy(
        headers = obtained.headers.filter { case (k, _) =>
          k == "Content-Type"
        }
      ),
      expected = expected
    )
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
        url = "http://localhost:8080/bar.html",
        headers = Map.empty,
        body = None
      )

    assertEquals(
      obtained = obtained.copy(
        headers = obtained.headers.filter { case (k, _) =>
          k == "Content-Type"
        }
      ),
      expected = expected
    )
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
        url = "http://localhost:8080/baz/raz.css",
        headers = Map.empty,
        body = None
      )

    assertEquals(
      obtained = obtained.copy(
        headers = obtained.headers.filter { case (k, _) =>
          k == "Content-Type"
        }
      ),
      expected = expected
    )
  }
}
