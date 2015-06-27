package servlets

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TestServlet extends HttpServlet {

  override def doGet(req: HttpServletRequest, res: HttpServletResponse) {
    val x = req.getParameter("x").toInt
    val y = remote.get("http://localhost:8081/test?x=" + x).toInt
    res.setStatus(200)
    res.getWriter.write(x.toString + "! is " + y.toString)
  }

}
