import java.sql.Connection
import scalaz.zio.ZIO
import scalaz.zio.DefaultRuntime
import scalaz.zio.Task
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

class ZioServlet extends HttpServlet with DefaultRuntime {

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
    unsafeRun {

      val s = c.createStatement()
      s.executeUpdate {
        """|create table if not exists
                          |  amounts ( id int not null auto_increment
                          |          , amount int not null
                          |          , primary key (id)
                          |          )
                          |""".stripMargin
      }
      s.close()
    }
  }

  def get(req: HttpServletRequest, res: HttpServletResponse) {
  }

}
