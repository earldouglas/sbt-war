import org.junit.Assert.assertEquals
import org.junit.Test

class TestSuite {

  @Test
  def getRoot(): Unit = {
    assertEquals(
      Response(
        200,
        Map("Content-Type" -> "text/html;charset=utf-8"),
        body = """|<html>
                       |  <body>
                       |    <h1>Hello, world!</h1>
                       |  </body>
                       |</html>""".stripMargin
      ),
      Request("GET", "http://localhost:8080/", Map.empty, None)
    )
  }
}
