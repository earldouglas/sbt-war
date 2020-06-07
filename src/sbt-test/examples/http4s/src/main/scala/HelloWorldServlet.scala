import cats.effect.IO
import org.http4s.implicits._
import org.http4s.server.DefaultServiceErrorHandler
import org.http4s.servlet.AsyncHttp4sServlet
import org.http4s.servlet.NonBlockingServletIo
import scala.concurrent.ExecutionContext

class HelloWorldServlet
  extends AsyncHttp4sServlet[IO](
    service = Services.helloWorldService.orNotFound,
    servletIo = NonBlockingServletIo(4096),
    serviceErrorHandler = DefaultServiceErrorHandler
  )(IO.ioConcurrentEffect(IO.contextShift(ExecutionContext.global)))
