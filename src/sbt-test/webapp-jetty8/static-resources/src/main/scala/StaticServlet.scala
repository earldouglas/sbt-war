package static

import javax.servlet._
import javax.servlet.http._

class StaticServlet extends HttpServlet {

  override def service(req: HttpServletRequest, res: HttpServletResponse) {
    if (req.getRequestURI.endsWith("/context")) static("/static1", res)
    else if (req.getRequestURI.endsWith("/jar")) static("/static2", res)
  }

  def static(name: String, res: HttpServletResponse): Unit = {
    val input = getServletContext.getResourceAsStream(name)
    val output = res.getOutputStream
    Iterator.continually(input.read).takeWhile(-1 !=).foreach(output.write)
    input.close
  }

}
