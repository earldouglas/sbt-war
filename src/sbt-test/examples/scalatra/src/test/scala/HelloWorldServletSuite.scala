import org.scalatest.FunSuiteLike
import org.scalatest.Matchers
import org.scalatra.test.scalatest.ScalatraSuite

class HelloWorldServletSuite extends ScalatraSuite with FunSuiteLike with Matchers {

  addServlet(classOf[HelloWorldServlet], "/*")

  test("get /") {
    get(uri = "/") {
      status shouldBe 200
      response.headers.get("Content-Type") shouldBe
        Some(List("text/html; charset=UTF-8"))
      body shouldBe
        """<html>
          |  <body>
          |    <h1>Hello, world!</h1>
          |  </body>
          |</html>""".stripMargin
    }
  }
}
