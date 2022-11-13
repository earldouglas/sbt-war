trait WithConnection {
  def connection: java.sql.Connection
}

trait WithRequest {
  def request: javax.servlet.http.HttpServletRequest
}

trait WithResponse {
  def response: javax.servlet.http.HttpServletResponse
}

object `package` {

  import zio.ZIO

  type DB[A] = ZIO[WithConnection, Throwable, A]
  type Req[A] = ZIO[WithRequest, Throwable, A]
  type Res[A] = ZIO[WithResponse, Throwable, A]
}
