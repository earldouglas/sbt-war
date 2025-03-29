package runners

import com.typesafe.scalalogging.LazyLogging
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@WebServlet(urlPatterns = Array("/test"))
class TestServlet extends HttpServlet with LazyLogging:

  override def doGet(
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit =
    logger.info("doGet")
    response.setCharacterEncoding("UTF-8")
    response.setContentType("text/html")
    response.getWriter.write("""<h1>Testing!</h1>""")
