import org.scalatest._

class TestSuite extends FunSuite with Matchers {

  test("/") {

    val response =
      Request("GET", "http://localhost:8080/", Map.empty, None)

    response.copy(headers = response.headers.mapValues(_.toLowerCase()).toMap) shouldBe
      Response(200, Map("Content-Type" -> "text/html;charset=utf-8"),
        body = 
          """|<html>
             |  <body>
             |    <h1>Hello, world!</h1>
             |  </body>
             |</html>""".stripMargin
      )
  }
}
