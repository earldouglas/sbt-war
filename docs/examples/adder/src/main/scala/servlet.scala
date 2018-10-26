import java.sql.Connection
import java.sql.DriverManager
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.Try

class AdderServlet extends HttpServlet {

  private val c: Connection = {
    Class.forName(sys.env("DB_DRIVER"))
    val c: Connection =
      DriverManager.getConnection( sys.env("DB_URL")
                                 , sys.env("DB_USER")
                                 , sys.env("DB_PASS")
                                 )
    c.setAutoCommit(false)
    c
  }

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
    Service.unsafeRunSync(Adder.init, c, 1000)
    updater.start
  }

  override def destroy(): Unit = {
    updater.interrupt
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
