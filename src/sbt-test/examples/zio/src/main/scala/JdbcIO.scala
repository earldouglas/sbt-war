import java.sql.Connection

trait JdbcIO {
  def connection: Connection
}

object JdbcIO {

  import java.sql.SQLException
  import zio.ZIO

  def effect[A](k: Connection => A): ZIO[JdbcIO, Throwable, A] =
    ZIO.fromFunctionM { env => ZIO.effect { k(env.connection) } }

  def transact[R <: JdbcIO, A](k: ZIO[R, Throwable, A]): ZIO[R, Throwable, A] =
    ZIO.fromFunction({ env: JdbcIO =>
      env.connection.setAutoCommit(false)
      env
    }).flatMap { env =>
      k.map({ a =>
        env.connection.commit()
        env.connection.close()
        a
      }).catchSome({
        case x: SQLException =>
          env.connection.rollback()
          env.connection.close()
          ZIO.fail(x)
      })
    }
}
