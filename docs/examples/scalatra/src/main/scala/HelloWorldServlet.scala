import org.scalatra.ScalatraServlet

class HelloWorldServlet extends ScalatraServlet {

  get("/") {

    contentType = "text/html; charset=utf-8"

    """<html>
      |  <body>
      |    <h1>Hello, world!</h1>
      |  </body>
      |</html>""".stripMargin
  }

}
