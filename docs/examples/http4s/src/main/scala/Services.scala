import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

object Services {

  val helloWorldService =
    HttpRoutes.of[IO] {
      case GET -> Root / "hello" / name =>
        Ok(s"Hello, $name.")
    }
}
