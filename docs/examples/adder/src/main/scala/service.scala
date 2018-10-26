import java.sql.Connection
import java.sql.PreparedStatement
import scala.concurrent.Future
import scala.concurrent.{ ExecutionContext => EC }
import scala.util.Failure
import scala.util.Success
import scala.util.Try

object `package` {
  type JDBC[A] = Connection => A
  type Error = String
}

object Service {

  def unsafeRun[A]( s: Service[A]
                  , c: Connection
                  )(implicit ec: EC): Future[Either[Error, A]] =
    c synchronized {
      val ea =
        s.run(c) map {
          case r@Right(_) =>
            c.commit()
            r
          case l@Left(_) =>
            c.rollback()
            l
        }
      ea
    }

  def success[A](a: => A)(implicit ec: EC): Service[A] =
    Service(_ => Future(Right(a)))

  def failure[A](e: => String)(implicit ec: EC): Service[A] =
    Service(_ => Future(Left(e)))

  def update(u: String)(implicit ec: EC): Service[Unit] =
    Service { c =>
      Future {
        Try {
          val s = c.createStatement()
          s.executeUpdate(u)
          s.close()
        } match {
          case Success(_) => Right(())
          case Failure(t) => Left(t.toString)
        }
      }
    }

  def update(q: String, k: PreparedStatement => Unit)(implicit ec: EC): Service[Unit] =
    Service { c =>
      Future {
        Try {
          val s = c.prepareStatement(q)
          k(s)
          s.executeUpdate()
          s.close()
        } match {
          case Success(_) => Right(())
          case Failure(t) => Left(t.toString)
        }
      }
    }

  def query[A](q: String, k: PreparedStatement => A)(implicit ec: EC): Service[A] =
    Service { c =>
      Future {
        Try {
          val s = c.prepareStatement(q)
          val a = k(s)
          s.close()
          a
        } match {
          case Success(x) => Right(x)
          case Failure(t) => Left(t.toString)
        }
      }
    }

}

case class Service[A](run: JDBC[Future[Either[Error, A]]]) {

  def map[B](f: A => B)(implicit ec: EC): Service[B] =
    Service { c =>
      run(c) map {
        case Left(es) => Left(es)
        case Right(a) => Right(f(a))
      }
    }

  def flatMap[B](f: A => Service[B])(implicit ec: EC): Service[B] =
    Service { c =>
      run(c) flatMap {
        case Left(es) => Future(Left(es))
        case Right(a) => f(a).run(c)
      }
    }

  def >>[B](x: Service[B])(implicit ec: EC): Service[B] =
    flatMap { _ => x }

}
