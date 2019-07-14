import com.jolbox.bonecp.BoneCP
import com.jolbox.bonecp.BoneCPConfig
import java.sql.Connection
import zio.ZIO

object Database {

  private val connectionPool: BoneCP = {
    Class.forName(sys.env("DB_DRIVER"))
    val config = new BoneCPConfig()
    config.setJdbcUrl(sys.env("DB_URL"))
    config.setUsername(sys.env("DB_USER"))
    config.setPassword(sys.env("DB_PASS"))
    config.setMinConnectionsPerPartition(5)
    config.setMaxConnectionsPerPartition(10)
    config.setPartitionCount(1)
    new BoneCP(config)
  }

  def c: Connection = {
    val c: Connection =
      connectionPool.getConnection
    c.setAutoCommit(false)
    c
  }

  def init: Connection => Unit = { c =>
    val s = c.createStatement()
    s.executeUpdate {
      """|create table if not exists
         |  guestbook ( id int not null auto_increment
         |            , name varchar(8) not null
         |            , message text not null
         |            , primary key (id)
         |            )
         |""".stripMargin
    }
    s.close()
  }

  def addEntry(name: String, message: String): DB[Unit] =
    ZIO.fromFunctionM { e =>
      ZIO {
        val s = "insert into guestbook (name, message) values (?, ?)"
        val stmt = e.connection.prepareStatement(s)
        stmt.setString(1, name)
        stmt.setString(2, message)
        stmt.executeUpdate
        stmt.close
      }
    }

  val getEntries: DB[List[(String, String)]] =
    ZIO.fromFunctionM { e =>
      ZIO.effect {
        val q = "select name, message from guestbook order by id asc"
        val stmt = e.connection.createStatement
        val rs = stmt.executeQuery(q)

        def _entries(acc: List[(String, String)]): List[(String, String)] =
          if (rs.next()) {
            val entry = (rs.getString("name"), rs.getString("message"))
            _entries(entry :: acc)
          } else {
            stmt.close
            acc
          }

        _entries(Nil)
      }
    }
}
