import org.scalatra.ScalatraServlet

class HelloWorldServlet extends ScalatraServlet {

  get("/") {
    """<html>
      |  <body>
      |    <h1>Hello, world!</h1>
      |  </body>
      |</html>""".stripMargin
  }

}
