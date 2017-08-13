package servlets

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TestServlet extends HttpServlet {

  override def doGet(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    res.setStatus(200)
  }

}

class H2G2Servlet extends HttpServlet {

  override def doGet(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    val h2g2 = System.getProperty("h2g2", "200")
    res.setStatus(h2g2.toInt)
  }

}

class AnnotationServlet extends HttpServlet {

  var initialized: Boolean = false

  @javax.annotation.PostConstruct
  private def initializeService(): Unit = { 
    initialized = true
  }

  override def doGet(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    res.setStatus(if (initialized) 200 else 404)
  }

}
