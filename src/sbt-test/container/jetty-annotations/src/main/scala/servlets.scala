package servlets

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TestServlet extends HttpServlet {

  private var status = 404

  @javax.annotation.PostConstruct
  private def updateStatus(): Unit = { 
    status = 200
  }

  override def doGet(req: HttpServletRequest, res: HttpServletResponse) {
    res.setStatus(status)
    res.getWriter.write("This is " + getClass.getName)
  }

}
