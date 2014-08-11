package servlets

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TestServlet extends HttpServlet {

  override def doGet(req: HttpServletRequest, res: HttpServletResponse) {
    val h2g2 = System.getProperty("h2g2", "200")
    res.setStatus(h2g2.toInt)
    res.getWriter.write("This is " + getClass.getName)
  }

}
