package drivers.db:

  import cats.Applicative
  import cats.Functor.ops.toAllFunctorOps
  import domain.entities.Count

  import java.sql.Connection

  trait Transactor[F[_]]:
    def transact[A](k: Connection => F[A]): F[A]

  def init[F[_]: Transactor: Applicative]: F[Unit] =
    summon[Transactor[F]].transact: c =>
      summon[Applicative[F]].pure:
        val s1 = c.createStatement()
        s1.executeUpdate(
          """|CREATE TABLE DATA
             |  ( `KEY` VARCHAR(256) NOT NULL
             |  , `VALUE` VARCHAR(256) NOT NULL
             |  , PRIMARY KEY (`KEY`)
             |  )
             |""".stripMargin
        )
        s1.close()

        val s2 = c.createStatement()
        s2.executeUpdate(
          """|INSERT INTO DATA
             |  ( `KEY`
             |  , `VALUE`
             |  )
             |  VALUES
             |  ( 'COUNT'
             |  , '0'
             |  )
             |""".stripMargin
        )
        s2.close()

  def counter[F[_]: Transactor: Applicative]: usecases.Counter[F] =
    new usecases.Counter[F]:

      override def counter: domain.operations.Counter[F] =
        new domain.operations.Counter[F]:

          override def increment: F[Unit] =
            summon[Transactor[F]].transact: c =>
              for oldCount <- get
              yield
                val stmt =
                  c.prepareStatement(
                    """|UPDATE DATA
                       |  SET `VALUE` = ?
                       |  WHERE `KEY` = 'COUNT'
                       |""".stripMargin
                  )
                val newValue = oldCount.value + 1
                stmt.setInt(1, newValue)
                stmt.executeUpdate()
                stmt.close()

          override def get: F[Count] =
            summon[Transactor[F]].transact: c =>
              summon[Applicative[F]].pure:
                val rs =
                  c.createStatement.executeQuery(
                    """|SELECT `VALUE`
                       |  FROM DATA
                       |  WHERE `KEY` = 'COUNT'
                       |""".stripMargin
                  )
                rs.next()
                val count = rs.getInt("VALUE")
                Count(count)
