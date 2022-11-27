import java.sql.Connection

trait JdbcIO {
  def connection: Connection
}

object JdbcIO {

  import java.sql.SQLException
  import zio.ZEnvironment
  import zio.ZIO

  def effect[A](k: Connection => A): ZIO[JdbcIO, Throwable, A] =
    ZIO.environmentWithZIO(env => ZIO.attempt(k(env.get.connection)))

  def transact[R <: JdbcIO, A](
      k: ZIO[R, Throwable, A]
  ): ZIO[R, Throwable, A] =
    ZIO
      .environmentWith({ env: ZEnvironment[JdbcIO] =>
        env.get.connection.setAutoCommit(false)
        env
      })
      .flatMap { env =>
        k.map({ a =>
          env.get.connection.commit()
          env.get.connection.close()
          a
        }).catchSome({ case x: SQLException =>
          env.get.connection.rollback()
          env.get.connection.close()
          ZIO.fail(x)
        })
      }
}
