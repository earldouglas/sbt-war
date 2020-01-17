package servlets

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TestServlet extends HttpServlet {

  override def doGet( req: HttpServletRequest
                    , res: HttpServletResponse
                    ): Unit = {
    res.setStatus(200)
  }
}
