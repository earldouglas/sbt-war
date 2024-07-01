package runners

import java.sql.Connection
import java.sql.DriverManager
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class MainServlet extends HttpServlet with Main:

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
