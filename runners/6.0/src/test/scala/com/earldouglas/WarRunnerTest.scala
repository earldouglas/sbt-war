package com.earldouglas

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class WarRunnerTest
    extends AnyFunSuite
    with Matchers
    with BeforeAndAfterAll {

  override def beforeAll(): Unit = {

    new com.earldouglas.HelloServlet()

    val thread: Thread =
      new Thread {
        override def run(): Unit = {
          WarRunner.main(Array("src/test/resources/war.properties"))
        }
      }
    thread.start()

    def isOpen(port: Int): Boolean =
      try {
        import java.net.Socket
        import java.net.InetSocketAddress
        val socket: Socket = new Socket()
        socket.connect(new InetSocketAddress("localhost", port))
        socket.close()
        true
      } catch {
        case e: Exception => false
      }

    def awaitOpen(port: Int, retries: Int = 40): Unit =
      if (!isOpen(port)) {
        if (retries > 0) {
          Thread.sleep(250)
          awaitOpen(port, retries - 1)
        } else {
          throw new Exception(s"expected port $port to be open")
        }
      }

    awaitOpen(8988)
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
        url = "http://localhost:8988/foo.html",
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
        url = "http://localhost:8988/bar.html",
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
        url = "http://localhost:8988/baz/raz.css",
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
        url = "http://localhost:8988/hello",
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
        url = "http://localhost:8988/annotation",
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
