package runners

import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CountServlet extends HttpServlet with Main:

  import java.sql.Connection
  import java.sql.DriverManager

  override lazy val c: Connection =
    Class.forName("org.h2.Driver")
    val connection: Connection =
      DriverManager.getConnection("jdbc:h2:mem:", "sa", "")
    connection.setAutoCommit(false)
    connection

  override def init(): Unit =
    super.init()

  override def destroy(): Unit =
    super.destroy()

  override def doGet(
      req: HttpServletRequest,
      res: HttpServletResponse
  ): Unit =
    res.addHeader("Content-Type", "application/json")
    res.getWriter().write(unsafeIncrementAndGetAsJson())

@WebServlet(urlPatterns = Array("/hello"))
class HelloServlet extends HttpServlet:

  override def doGet(
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit =
    response.setCharacterEncoding("UTF-8")
    response.setContentType("text/html")
    response.getWriter.write("""<h1>Hello, world!</h1>""")
