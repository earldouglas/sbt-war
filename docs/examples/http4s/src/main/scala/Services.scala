import cats.effect.IO
import org.http4s.HttpService
import org.http4s.dsl.io._

object Services {

  val helloWorldService = HttpService[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")
  }

}
