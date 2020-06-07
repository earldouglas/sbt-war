trait ~>[F[_], G[_]] {
  def apply[A](f: F[A]): G[A]
}

trait Monad[F[_]] {
  def pure[A](a: A): F[A]
  def bind[A,B](fa: F[A])(f: A => F[B]): F[B]
}

sealed trait Free[F[_], A] {

  import Free._

  def map[B](f: A => B): Free[F, B] = flatMap { a => pure(f(a)) }

  def flatMap[B](f: A => Free[F, B]): Free[F, B] = Bind(this, f)

  def foldMap[G[_]: Monad](nt: F ~> G): G[A] =
    this match {
      case Pure(a) => implicitly[Monad[G]].pure(a)
      case Suspend(fa) => nt(fa)
      case Bind(fa, f) => val mg = implicitly[Monad[G]]
                          val ga = fa.foldMap(nt)
                          mg.bind(ga)(f(_).foldMap(nt))
    }

}

object Free {

  def pure[F[_], A](a: A): Free[F, A] = Pure(a)

  def liftM[F[_], A](fa: F[A]): Free[F, A] = Suspend(fa)

  final case class Pure[F[_], A](a: A) extends Free[F, A]
  final case class Suspend[F[_], A](fa: F[A]) extends Free[F, A]
  final case class Bind[F[_], A, B]( fa: Free[F, A]
                                   , f: A => Free[F, B]
                                   ) extends Free[F, B]
}
