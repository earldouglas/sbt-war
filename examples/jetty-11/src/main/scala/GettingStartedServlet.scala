import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class GettingStartedServlet extends HttpServlet {

  override def doGet(
      req: HttpServletRequest,
      res: HttpServletResponse
  ) {

    res.setContentType("text/html")
    res.setCharacterEncoding("UTF-8")

    val responseBody: String =
      """|<html>
         |  <body>
         |    <h1>Hello, world!</h1>
         |  </body>
         |</html>""".stripMargin
    res.getWriter.write(responseBody)
  }
}
