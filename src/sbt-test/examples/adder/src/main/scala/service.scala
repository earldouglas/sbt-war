import java.sql.Connection
import java.sql.PreparedStatement
import scala.concurrent.Future
import scala.concurrent.{ ExecutionContext => EC }
import scala.util.Failure
import scala.util.Success
import scala.util.Try

object `package` {
  type JDBC[A] = Connection => A
  type Error = (Int, String)
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

  def apply[A](a: => A)(implicit ec: EC): Service[A] =
    Service { _ =>
      Future {
        Try(a) match {
          case Failure(t) => Left((500, t.getMessage))
          case Success(x) => Right(x)
        }
      }
    }

  def apply[A](k: Connection => A)(implicit ec: EC): Service[A] =
    Service { c =>
      Future {
        Try(k(c)) match {
          case Failure(t) => Left((500, t.getMessage))
          case Success(x) => Right(x)
        }
      }
    }

  def update(u: String)(implicit ec: EC): Service[Unit] =
    apply { c =>
      val s = c.createStatement()
      s.executeUpdate(u)
      s.close()
    }

  def update(q: String, k: PreparedStatement => Unit)(implicit ec: EC): Service[Unit] =
    apply { c =>
      val s = c.prepareStatement(q)
      k(s)
      s.executeUpdate()
      s.close()
    }

  def query[A](q: String, k: PreparedStatement => A)(implicit ec: EC): Service[A] =
    apply { c =>
      val s = c.prepareStatement(q)
      val a = k(s)
      s.close()
      a
    }
}

case class Service[A](run: JDBC[Future[Either[Error, A]]]) {

  def map[B](f: A => B)(implicit ec: EC): Service[B] =
    Service { c =>
      run(c) map {
        case Left(e) => Left(e)
        case Right(a) => Right(f(a))
      }
    }

  def mapLeft(f: Error => Error)(implicit ec: EC): Service[A] =
    Service { c =>
      run(c) map {
        case Left(e) => Left(f(e))
        case Right(a) => Right(a)
      }
    }

  def withLeft(f: Error => Unit)(implicit ec: EC): Service[A] =
    Service { c =>
      run(c) map {
        case Left(e) => f(e) ; Left(e)
        case Right(a) => Right(a)
      }
    }

  def flatMap[B](f: A => Service[B])(implicit ec: EC): Service[B] =
    Service { c =>
      run(c) flatMap {
        case Left(e) => Future(Left(e))
        case Right(a) => f(a).run(c)
      }
    }

  def >>[B](x: Service[B])(implicit ec: EC): Service[B] =
    flatMap { _ => x }

}
