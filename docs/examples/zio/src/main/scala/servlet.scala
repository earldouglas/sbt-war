import java.sql.Connection
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import zio.Runtime
import zio.ZIO
import zio.internal.PlatformLive
import com.earldouglas.zio.jdbc.JdbcIO

class ZioServlet extends HttpServlet {

  def unsafeRun[A](k: ZIO[JdbcIO, Throwable, A]): A = {
    val runtime: Runtime[JdbcIO] =
      Runtime( new JdbcIO {
                 val connection: Connection =
                   Database.connectionPool.getConnection
               }
             , PlatformLive.Default
             )
    runtime.unsafeRun(JdbcIO.transact(k))
  }

  override def init: Unit = {
    unsafeRun(Database.init)
  }

  override def doGet( req: HttpServletRequest
                    , res: HttpServletResponse
                    ): Unit = {
    res.setContentType("text/html;charset=UTF-8")
    unsafeRun {
      Database.getEntries map { entries =>
        res.getWriter.write("<ul>\n")
        entries map { case (name, message) =>
          res.getWriter.write(s"  <li>${name}: ${message}</li>\n")
        }
        res.getWriter.write("</ul>\n")
      }
    }
  }

  override def doPost( req: HttpServletRequest
                     , res: HttpServletResponse
                     ): Unit = {

    val entry: Option[(String, String)] =
      for {
        name    <- Option(req.getParameter("name"))
        message <- Option(req.getParameter("message"))
      } yield (name, message)

    entry match {
      case Some((name, message)) =>
        unsafeRun(Database.addEntry(name, message))
        res.sendRedirect("/")
      case None =>
        res.setStatus(400)
    }
  }
}
