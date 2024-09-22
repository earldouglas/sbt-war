package domain.operations:

  import domain.entities.Count

  trait Counter[F[_]]:

    def increment: F[Unit]
    def get: F[Count]
