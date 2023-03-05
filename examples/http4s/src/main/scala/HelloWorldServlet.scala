import cats.effect.IO
import cats.effect.std.Dispatcher
import cats.effect.unsafe.IORuntime
import org.http4s.implicits._
import org.http4s.server.DefaultServiceErrorHandler
import org.http4s.servlet.AsyncHttp4sServlet
import org.http4s.servlet.NonBlockingServletIo
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration

class HelloWorldServlet
    extends AsyncHttp4sServlet[IO](
      httpApp = Services.helloWorldService.orNotFound,
      asyncTimeout = Duration.Inf,
      servletIo = NonBlockingServletIo(4096),
      serviceErrorHandler = DefaultServiceErrorHandler,
      dispatcher = new Dispatcher[IO] {
        override def unsafeToFutureCancelable[A](fa: IO[A]): (Future[A], () => Future[Unit]) =
          (fa.unsafeToFuture()(IORuntime.global), () => Future(()))
      }
    )(IO.asyncForIO)
