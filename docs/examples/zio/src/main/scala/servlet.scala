import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scalaz.zio.Runtime
import scalaz.zio.ZIO
import scalaz.zio.internal.PlatformLive

object Database {

  type DB[A] = ZIO[Connection, Throwable, A]

  val c: Connection = {
    Class.forName(sys.env("DB_DRIVER"))
    val c: Connection =
      DriverManager.getConnection( sys.env("DB_URL")
                                 , sys.env("DB_USER")
                                 , sys.env("DB_PASS")
                                 )
    c.setAutoCommit(false)
    c
  }

  def unsafeRun[A](db: DB[A]): Either[SQLException, A] = {
    val rt: Runtime[Connection] =
      Runtime(Database.c, PlatformLive.Default)
    rt.unsafeRun {
      db.map({ a =>
        c.commit()
        Right(a)
      }).catchSome {
        case e: SQLException =>
          c.rollback()
          ZIO.succeed(Left(e))
      }
    }
  }

  def init: DB[Unit] =
    ZIO.fromFunctionM { c =>
      ZIO.effect {
        val s = c.createStatement()
        s.executeUpdate {
          """|create table if not exists
             |  guestbook ( id int not null auto_increment
             |            , name varchar(8) not null
             |            , message text not null
             |            , primary key (id)
             |            )
             |""".stripMargin
        }
        s.close()
      }
    }

  def addEntry(name: String, message: String): DB[Unit] =
    ZIO.fromFunctionM { c =>
      ZIO {
        val s = "insert into guestbook (name, message) values (?, ?)"
        val stmt = c.prepareStatement(s)
        stmt.setString(1, name)
        stmt.setString(2, message)
        stmt.executeUpdate
        stmt.close
      }
    }

  val getEntries: DB[List[(String, String)]] =
    ZIO.fromFunctionM { c =>
      ZIO.effect {
        val q = "select name, message from guestbook order by id asc"
        val stmt = c.createStatement
        val rs = stmt.executeQuery(q)

        def _entries(acc: List[(String, String)]): List[(String, String)] =
          if (rs.next()) {
            val entry = (rs.getString("name"), rs.getString("message"))
            _entries(entry :: acc)
          } else {
            stmt.close
            acc
          }

        _entries(Nil)
      }
    }
  }

class ZioServlet extends HttpServlet {

  override def init: Unit = {
    Database.unsafeRun(Database.init)
  }

  override def doGet( req: HttpServletRequest
                    , res: HttpServletResponse
                    ): Unit = {
    res.setContentType("text/html;charset=UTF-8")
    Database.unsafeRun {
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
        Database.unsafeRun(Database.addEntry(name, message))
        res.sendRedirect("/")
      case None =>
        Database.unsafeRun { 
          for {
            _ <- Database.addEntry("nobody", "these")
            _ <- Database.addEntry("nobody", "inserts")
            _ <- Database.addEntry("nobody", "will")
            _ <- Database.addEntry("nobody", "be")
            _ <- Database.addEntry("nobody", "rolled")
            _ <- Database.addEntry("thisnameistoolong", "oh noes!")
            _ <- Database.addEntry("nobody", "back")
          } yield ()
        }
        res.setStatus(400)
    }

  }

}
