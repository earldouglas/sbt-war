import java.sql.Connection
import java.sql.DriverManager
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.servlet.AsyncContext
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.io.Source

trait JdbcServlet {

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

  def unsafeRun[A](ctx: AsyncContext)(s: Service[A]): Future[Unit] =
    Service.unsafeRun(s, c) map { _ => ctx.complete() }
}

trait CommandServlet extends HttpServlet with JdbcServlet {

  override def doPost( req: HttpServletRequest
                     , res: HttpServletResponse
                     ) {
    unsafeRun(req.startAsync()) {
      Service {
        Source.fromInputStream(req.getInputStream)
              .mkString
              .toInt
      } mapLeft { _ =>
        (400, "couldn't parse number")
      } flatMap { amount =>
        Adder.add(amount)
      } map { _ =>
        res.setStatus(201)
      } withLeft { case (status, message) =>
        res.setContentType("text/plain")
        res.setStatus(status)
        res.getWriter.write(message)
      }
    }
  }
}

trait UpdateServlet extends HttpServlet with JdbcServlet {

  private val updater: Thread =
    new Thread {
      override def run(): Unit = {
        while (!isInterrupted) {
          Service.unsafeRun(Adder.update, c)
          Thread.sleep(250)
        }
      }
    }

  override def init(): Unit = {
    updater.start
    super.init()
  }

  override def destroy(): Unit = {
    updater.interrupt
    super.destroy()
  }
}

trait QueryServlet extends HttpServlet with JdbcServlet {

  override def doGet( req: HttpServletRequest
                    , res: HttpServletResponse
                    ) {
    unsafeRun(req.startAsync()) {
      Adder.getSum map { case (_, result) =>
        res.setContentType("text/plain")
        res.setCharacterEncoding("UTF-8")
        res.getWriter.write(s"${result}\n")
      } withLeft { case (status, message) =>
        res.setContentType("text/plain")
        res.setStatus(status)
        res.getWriter.write(message)
      }
    }
  }
}

class AdderServlet extends CommandServlet
                   with UpdateServlet
                   with QueryServlet {

  override def init(): Unit = {
    Await.result( Service.unsafeRun(Adder.init, c)
                , Duration(5000, MILLISECONDS)
                )
    super.init()
  }

  override def destroy(): Unit = {
    super.destroy()
  }
}
