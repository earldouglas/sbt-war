import com.earldouglas.zio.jdbc.JdbcIO
import com.jolbox.bonecp.BoneCP
import com.jolbox.bonecp.BoneCPConfig
import zio.ZIO

object Database {

  val connectionPool: BoneCP = {
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

  def init: ZIO[JdbcIO, Throwable, Unit] =
    JdbcIO.effect { c =>
      val stmt = c.createStatement()
      stmt.executeUpdate {
        """|create table if not exists
           |  guestbook ( id int not null auto_increment
           |            , name varchar(255) not null
           |            , message text not null
           |            , primary key (id)
           |            )
           |""".stripMargin
      }
      stmt.close()
    }

  def addEntry(name: String, message: String): ZIO[JdbcIO, Throwable, Unit] =
    JdbcIO.effect { c =>
      val s = "insert into guestbook (name, message) values (?, ?)"
      val stmt = c.prepareStatement(s)
      stmt.setString(1, name)
      stmt.setString(2, message)
      stmt.executeUpdate
      stmt.close
    }

  val getEntries: ZIO[JdbcIO, Throwable, List[(String, String)]] =
    JdbcIO.effect { c =>
      val q = "select name, message from guestbook order by id asc"
      val stmt = c.createStatement
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
