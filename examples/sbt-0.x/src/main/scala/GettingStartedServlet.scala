import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class GettingStartedServlet extends HttpServlet {

  override def doGet(
      req: HttpServletRequest,
      res: HttpServletResponse
  ): Unit = {

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
