import java.sql.SQLException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import zio.Runtime
import zio.ZIO
import zio.internal.PlatformLive

object ServletEffects {

  def getParam(name: String): Req[Option[String]] =
    ZIO.fromFunctionM { e =>
      ZIO.effect {
        Option(e.request.getParameter(name))
      }
    }

  def setContentType(ct: String): Res[Unit] =
    ZIO.fromFunctionM { e =>
      ZIO.effect {
        e.response.setContentType(ct)
      }
    }

  def write(x: String): Res[Unit] =
    ZIO.fromFunctionM { e =>
      ZIO.effect {
        e.response.getWriter.write(x)
      }
    }

  def redirect(url: String): Res[Unit] =
    ZIO.fromFunctionM { e =>
      ZIO.effect {
        e.response.sendRedirect(url)
      }
    }

  def status(code: Int): Res[Unit] =
    ZIO.fromFunctionM { e =>
      ZIO.effect {
        e.response.setStatus(code)
      }
    }

}

object Services {

  import ServletEffects._

  def getMessages(): ZIO[ WithConnection with WithRequest with WithResponse
                        , Throwable
                        , Unit
                        ] =
    for {
      _       <- setContentType("text/html;charset=UTF-8")
      entries <- Database.getEntries
      _       <- write("<ul>\n")
      _       <- ZIO.foreach(entries) { case (name, message) =>
                   write(s"  <li>${name}: ${message}</li>\n")
                 }
      _       <- write("</ul>\n")
    } yield ()

  def postMessage(): ZIO[ WithConnection with WithRequest with WithResponse
                        , Throwable
                        , Unit
                        ] =
    for {
      nameO    <- getParam("name")
      messageO <- getParam("message")
      _        <- (nameO, messageO) match {
                    case (Some(name), Some(message)) =>
                      for {
                        _ <- Database.addEntry(name, message)
                        _ <- redirect("/")
                      } yield ()
                    case _ =>
                      for {
                        _ <- Database.addEntry("nobody", "these")
                        _ <- Database.addEntry("nobody", "inserts")
                        _ <- Database.addEntry("nobody", "will")
                        _ <- Database.addEntry("nobody", "be")
                        _ <- Database.addEntry("nobody", "rolled")
                        _ <- Database.addEntry("thisnameistoolong", "oh noes!")
                        _ <- Database.addEntry("nobody", "back")
                        _ <- redirect("/")
                      } yield ()
                    }
    } yield ()
}

class ZioServlet extends HttpServlet {

  type Env = WithConnection with WithRequest with WithResponse

  def unsafeRun[A]( req: HttpServletRequest
                  , res: HttpServletResponse
                  )( k: ZIO[Env, Throwable, A]
                  ): Either[SQLException, A] = {
    val env: Env =
      new WithConnection with WithRequest with WithResponse {
        val connection = Database.c
        val request = req
        val response = res
      }
    val rt: Runtime[Env] = Runtime(env, PlatformLive.Default)
    rt.unsafeRun {
      k.map({ a =>
          println("COMMITTING TRANSACTION")
        env.connection.commit()
        env.connection.close()
        Right(a)
      }).catchSome {
        case e: SQLException =>
          println("ROLLING BACK TRANSACTION")
          env.connection.rollback()
          env.connection.close()
          ZIO.succeed(Left[SQLException, A](e))
      }
    }
  }

  override def init: Unit = {
    Database.init(Database.c)
  }

  override def doGet( req: HttpServletRequest
                    , res: HttpServletResponse
                    ): Unit =
    unsafeRun(req, res)(Services.getMessages)

  override def doPost( req: HttpServletRequest
                     , res: HttpServletResponse
                     ): Unit =
    unsafeRun(req, res)(Services.postMessage)
}
