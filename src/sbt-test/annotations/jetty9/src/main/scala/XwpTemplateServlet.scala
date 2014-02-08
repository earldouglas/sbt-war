package com.earldouglas.xwptemplate

import scala.xml.NodeSeq
import javax.servlet.http.HttpServlet

class XwpTemplateServlet extends HttpServlet {

  import javax.servlet.http.HttpServletRequest
  import javax.servlet.http.HttpServletResponse

  private var status = "PostConstruct has NOT been called. :["

  @javax.annotation.PostConstruct
  private def updateStatus(): Unit = { 
    status = "PostConstruct HAS been called!!!1 :]"
  }

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {

    response.setContentType("text/html")
    response.setCharacterEncoding("UTF-8")

    val responseBody: NodeSeq = <html><body><h1>{ status }</h1></body></html>
    response.getWriter.write(responseBody.toString)
  }
}
