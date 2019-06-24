import org.scalatest.FunSuiteLike
import org.scalatra.test.scalatest.ScalatraSuite

class HelloWorldServletSuite extends ScalatraSuite with FunSuiteLike {

  addServlet(classOf[HelloWorldServlet], "/*")

  test("get /") {
    get(uri = "/") {
      status should equal (200)
      response.headers.get("Content-Type") should equal {
        Some(List("text/html; charset=UTF-8"))
      }
      body should equal {
        """<html>
          |  <body>
          |    <h1>Hello, world!</h1>
          |  </body>
          |</html>""".stripMargin
      }
    }
  }
}
