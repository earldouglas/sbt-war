class GettingStartedServlet extends javax.servlet.http.HttpServlet {

  override def doGet( req: javax.servlet.http.HttpServletRequest
                    , res: javax.servlet.http.HttpServletResponse
                    ) {

    res.setContentType("text/html")
    res.setCharacterEncoding("UTF-8")

    val responseBody: String =
      """<html>
        |  <body>
        |    <h1>Hello, world!</h1>
        |    <a href="/">home</a>
        |  </body>
        |</html>""".stripMargin
    res.getWriter.write(responseBody)
  }
}
