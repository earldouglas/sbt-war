package servlets

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import maths._
import typeclasses.Show
import typeclasses.Show._

class TestServlet extends HttpServlet {

  override def doGet(req: HttpServletRequest, res: HttpServletResponse) {
    res.setStatus(200)
    res.getWriter.write("10! is " + factorial(10).show)
  }

}
