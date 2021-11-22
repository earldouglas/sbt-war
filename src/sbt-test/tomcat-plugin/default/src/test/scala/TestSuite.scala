import org.scalatest._

class TestSuite extends FunSuite with Matchers {

  test("/") {
    Request("GET", "http://localhost:8080/", Map.empty, None) shouldBe
      Response(
        200,
        Map(
          "Connection" -> "keep-alive",
          "Content-Type" -> "text/html;charset=UTF-8",
          "Keep-Alive" -> "timeout=60"
        ),
        """|<html>
           |  <body>
           |    <h1>Hello, world!</h1>
           |  </body>
           |</html>""".stripMargin
      )
  }
}
