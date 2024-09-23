package usecases:

  import cats.Monad
  import cats.syntax.flatMap.toFlatMapOps
  import cats.syntax.functor.toFunctorOps
  import domain.entities.Count

  trait Counter[F[_]]:

    def counter: domain.operations.Counter[F]

    def incrementAndGet(using M: Monad[F]): F[Count] =
      for
        _ <- counter.increment
        c <- counter.get
      yield c
