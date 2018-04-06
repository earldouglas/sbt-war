import cats.effect.IO
import org.http4s.server.DefaultServiceErrorHandler
import org.http4s.servlet.BlockingServletIo
import org.http4s.servlet.Http4sServlet

class HelloWorldServlet
  extends Http4sServlet[IO]( service = Services.helloWorldService
                           , servletIo = BlockingServletIo(4096)
                           , serviceErrorHandler = DefaultServiceErrorHandler
                           )
