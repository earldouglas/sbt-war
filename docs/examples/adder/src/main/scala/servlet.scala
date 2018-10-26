import java.sql.Connection
import java.sql.DriverManager
import javax.servlet.AsyncContext
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

trait JdbcServlet {

  def c: Connection

  def unsafeRun[A]( s: Service[A]
                  , ctx: AsyncContext
                  , onError: String => Unit = _ => ()
                  ): Future[Unit] =
    Service.unsafeRun(s, c) map {
      case Right(_) =>
      case Left(message) => onError(message)
    } map { _ => ctx.complete() }
}

trait CommandServlet extends HttpServlet with JdbcServlet {

  override def doPost( req: HttpServletRequest
                     , res: HttpServletResponse
                     ) {

    res.setStatus(201)
    res.setHeader("Location", "/")

    val reqBody: String =
      Source.fromInputStream(req.getInputStream).mkString

    unsafeRun(Adder.add(reqBody.toInt), req.startAsync())
  }
}

trait QueryServlet extends HttpServlet with JdbcServlet {

  def c: Connection

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

  override def doGet( req: HttpServletRequest
                    , res: HttpServletResponse
                    ) {

    res.setContentType("text/plain")
    res.setCharacterEncoding("UTF-8")

    unsafeRun( Adder.getSum map {
                 case ((_, result)) =>
                   res.getWriter.write(s"${result}\n")
               }
             , req.startAsync()
             , { message =>
                   res.setStatus(500)
                   res.getWriter.write(message)
               }
             )
  }
}

class AdderServlet extends CommandServlet with QueryServlet {

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

  override def init(): Unit = {
    Service.unsafeRun(Adder.init, c) map { _ => super.init() }
  }

  override def destroy(): Unit = {
    super.destroy()
  }
}
