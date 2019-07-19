import com.earldouglas.zio.jdbc.JdbcIO
import com.earldouglas.zio.servlet.Request
import com.earldouglas.zio.servlet.RequestIO
import com.earldouglas.zio.servlet.Response
import com.earldouglas.zio.servlet.ResponseIO
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import zio.Runtime
import zio.ZIO
import zio.internal.PlatformLive

object Services {

  val getMessages: ZIO[RequestIO with ResponseIO with JdbcIO, Throwable, Unit] =
    for {
      _       <- Response.setContentType("text/html;charset=UTF-8")
      entries <- Database.getEntries
      writer  <- Response.getWriter
      _        = writer.write("<ul>\n")
      _        = entries map { case (name, message) =>
                   writer.write(s"  <li>${name}: ${message}</li>\n")
                 }
      _        = writer.write("</ul>\n")
    } yield ()

  val addMessage: ZIO[RequestIO with ResponseIO with JdbcIO, Throwable, Unit] =
    for {
      nameO    <- Request.getParameter("name")
      messageO <- Request.getParameter("message")
      _        <- (nameO, messageO) match {
                    case (Some(name), Some(message)) =>
                      for {
                        _ <- Database.addEntry(name, message)
                        _ <- Response.sendRedirect("/")
                      } yield ()
                    case _ =>
                      for {
                        _ <- Response.setStatus(400)
                      } yield ()
                  }

    } yield ()
}

class ZioServlet extends HttpServlet {

  def unsafeRun[A](
       req: HttpServletRequest
     , res: HttpServletResponse
     )( k: ZIO[RequestIO with ResponseIO with JdbcIO, Throwable, A]
     ): Either[Throwable, A] = {

      val env =
        new RequestIO with ResponseIO with JdbcIO {
          Class.forName("org.h2.Driver")
          val request = req
          val response = res
          val connection = Database.connectionPool.getConnection
        }

      val runtime: Runtime[RequestIO with ResponseIO with JdbcIO] =
        Runtime(env, PlatformLive.Default)

      runtime.unsafeRun {
        JdbcIO.transact(k).map(a => Right(a)) catchAll { t =>
          ZIO.succeed(Left[Throwable, A](t))
        }
      }
    }

  override def init: Unit = {
    unsafeRun(null, null)(Database.init)
  }

  override def doGet( req: HttpServletRequest
                    , res: HttpServletResponse
                    ): Unit = {
    res.setContentType("text/html;charset=UTF-8")
    unsafeRun(req, res) {
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
        unsafeRun(req, res)(Database.addEntry(name, message))
        res.sendRedirect("/")
      case None =>
        res.setStatus(400)
    }
  }
}
