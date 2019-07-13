import java.sql.Connection
import java.sql.DriverManager
import zio.ZIO

object Database {

  val c: Connection = {
    Class.forName(sys.env("DB_DRIVER"))
    val c: Connection =
      DriverManager.getConnection( sys.env("DB_URL")
                                 , sys.env("DB_USER")
                                 , sys.env("DB_PASS")
                                 )
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
