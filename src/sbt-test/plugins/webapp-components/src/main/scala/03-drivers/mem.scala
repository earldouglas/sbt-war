package drivers.mem:

  import cats.Functor
  import cats.Functor.ops.toAllFunctorOps
  import cats.effect.kernel.Ref
  import cats.effect.kernel.Ref.Make
  import domain.entities.Count

  def counter[F[_]: Make: Functor]: F[usecases.Counter[F]] =
    for ref <- Ref.of[F, Int](0)
    yield new usecases.Counter[F]:

      override def counter =
        new domain.operations.Counter[F]:

          override def increment: F[Unit] =
            ref.update(x => x + 1)

          override def get: F[Count] =
            ref.get.map(x => Count(x))
