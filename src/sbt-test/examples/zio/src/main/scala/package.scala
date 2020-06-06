import java.sql.Connection
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import zio.ZIO

trait WithConnection {
  def connection: Connection
}

trait WithRequest {
  def request: HttpServletRequest
}

trait WithResponse {
  def response: HttpServletResponse
}

object `package` {

  type DB[A] = ZIO[WithConnection, Throwable, A]
  type Req[A] = ZIO[WithRequest, Throwable, A]
  type Res[A] = ZIO[WithResponse, Throwable, A]

}
