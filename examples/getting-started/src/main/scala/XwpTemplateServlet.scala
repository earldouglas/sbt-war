package com.earldouglas.xwptemplate

import javax.servlet.http.HttpServlet

class XwpTemplateServlet extends HttpServlet {

  import javax.servlet.http.HttpServletRequest
  import javax.servlet.http.HttpServletResponse

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {

    response.setContentType("text/html")
    response.setCharacterEncoding("UTF-8")

    val responseBody: String =
      """<html>
        |  <body>
        |    <h1>Hello, world!</h1>
        |    <a href=".">home</a>
        |  </body>
        |</html>""".stripMargin
    response.getWriter.write(responseBody)
  }
}
