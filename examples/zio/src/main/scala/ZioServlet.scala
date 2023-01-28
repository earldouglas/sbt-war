import javax.servlet.http.HttpServlet
import zio.ZIO

object Services {

  val getMessages: ZIO[
    WithRequest with WithResponse with JdbcIO,
    Throwable,
    Unit
  ] =
    for {
      _ <- Response.setContentType("text/html;charset=UTF-8")
      entries <- Database.getEntries
      writer <- Response.getWriter
      _ = writer.write("<ul>\n")
      _ = entries map { case (name, message) =>
        writer.write(s"  <li>${name}: ${message}</li>\n")
      }
      _ = writer.write("</ul>\n")
    } yield ()

  val addMessage: ZIO[
    WithRequest with WithResponse with JdbcIO,
    Throwable,
    Unit
  ] =
    for {
      nameO <- Request.getParameter("name")
      messageO <- Request.getParameter("message")
      _ <- (nameO, messageO) match {
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

  import javax.servlet.http.HttpServletRequest
  import javax.servlet.http.HttpServletResponse
  import zio.Runtime
  import zio.Unsafe
  import zio.ZLayer

  def unsafeRun[A](
      req: HttpServletRequest,
      res: HttpServletResponse
  )(
      k: ZIO[WithRequest with WithResponse with JdbcIO, Throwable, A]
  ): Either[Throwable, A] = {

    val requestEnv = new WithRequest { val request = req }
    val responseEnv = new WithResponse { val response = res }
    val jdbcEnv =
      new JdbcIO {
        Class.forName("org.h2.Driver")
        val connection = Database.connectionPool.getConnection
      }

    Unsafe.unsafe { implicit u: Unsafe =>
      Runtime.default.unsafe
        .run(
          JdbcIO
            .transact(k)
            .map(a => Right(a))
            .catchAll { t =>
              ZIO.succeed(Left[Throwable, A](t))
            }
            .provide(
              ZLayer.succeed(requestEnv) ++
                ZLayer.succeed(responseEnv) ++
                ZLayer.succeed(jdbcEnv)
            )
        )
        .getOrThrowFiberFailure()
    }
  }

  override def init: Unit = {
    unsafeRun(null, null)(Database.init)
  }

  override def service(
      req: HttpServletRequest,
      res: HttpServletResponse
  ): Unit = {
    try {
      unsafeRun(req, res) {
        Request.route {
          case "GET" :: Nil  => Services.getMessages
          case "POST" :: Nil => Services.addMessage
          case _             => Response.setStatus(404)
        }
      }
    } catch {
      case t: Throwable =>
        t.printStackTrace
        res.setStatus(500)
    }
  }
}
