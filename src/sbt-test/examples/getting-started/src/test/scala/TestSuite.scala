import org.scalatest._

class TestSuite extends HttpSuite with Matchers {

  test("/") {
    request(
      method = "GET",
      url = "http://localhost:8080/",
      headers = Map.empty,
      body = None
    ) shouldBe {
      Response(
        status = 200,
        headers = Map("Content-Type" -> "text/html;charset=utf-8"),
        body = 
          """|<html>
             |  <body>
             |    <h1>Hello, world!</h1>
             |  </body>
             |</html>""".stripMargin
      )
    }
  }
}
