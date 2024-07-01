package runners

import cats.effect.IO
import cats.effect.unsafe.implicits._
import drivers.db.Transactor

import java.sql.Connection
import scala.util.chaining.scalaUtilChainingOps

trait Main:

  def c: Connection

  given tx: Transactor[IO] with

    override def transact[A](k: Connection => IO[A]): IO[A] =
      k(c)
        .tap(_ => c.commit())
        .recoverWith:
          case e =>
            c.rollback()
            IO.raiseError(e)

  drivers.db.init.unsafeRunSync()

  val counter: usecases.Counter[IO] = drivers.db.counter[IO]

  def unsafeIncrementAndGet(): Int =
    counter.incrementAndGet.unsafeRunSync().value

  def unsafeIncrementAndGetAsJson(): String =
    s"""|{
        |  "count": ${unsafeIncrementAndGet()}
        |}
        |""".stripMargin
