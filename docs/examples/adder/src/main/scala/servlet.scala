import java.sql.Connection
import java.sql.DriverManager
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.Try

trait CommandServlet extends HttpServlet {

  def c: Connection

  override def doPost( req: HttpServletRequest
                     , res: HttpServletResponse
                     ) {

    res.setStatus(201)
    res.setHeader("Location", "/")

    val reqBody: String =
      Source.fromInputStream(req.getInputStream).mkString
    val s: Service[Unit] = Adder.add(reqBody.toInt)
    Service.unsafeRunSync(s, c, 1000)
  }
}

trait QueryServlet extends HttpServlet {

  def c: Connection

  private val updater: Thread =
    new Thread {
      override def run(): Unit = {
        while (!isInterrupted) {
          Try(Service.unsafeRunSync(Adder.update, c, 10000))
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

    Service.unsafeRunSync(Adder.getSum, c, 1000) match {
      case Right((_, result)) =>
        res.getWriter.write(s"${result}\n")
      case Left(message) =>
        res.setStatus(500)
        res.getWriter.write(message)
    }
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
    Service.unsafeRunSync(Adder.init, c, 1000)
    super.init()
  }

  override def destroy(): Unit = {
    super.destroy()
  }
}
