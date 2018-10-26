import scala.concurrent.{ ExecutionContext => EC }

object Adder {

  def init(implicit ec: EC): Service[Unit] =
    Service.update("""|create table if not exists
                      |  amounts ( id int not null auto_increment
                      |          , amount int not null
                      |          , primary key (id)
                      |          )
                      |""".stripMargin) >>
    Service.update("""|create table if not exists
                      |  sums ( last_id int not null unique
                      |       , sum int not null
                      |       )
                      |""".stripMargin)

  def add(amount: Int)(implicit ec: EC): Service[Unit] =
    Service.update( "insert into amounts (amount) values (?)"
                  , _.setInt(1, amount)
                  )

  def getAmounts(afterId: Int)(implicit ec: EC): Service[(Int, List[Int])] =
    Service.query( "select * from amounts where id > ? order by id asc"
                 , { s =>
                       s.setInt(1, afterId)
                       val r = s.executeQuery()
                       var amounts: List[Int] = Nil
                       var lastId: Int = afterId
                       while (r.next()) {
                         lastId = r.getInt("id")
                         amounts = r.getInt("amount") :: amounts
                       }
                       (lastId, amounts)
                     }
                 )

  def getSum(implicit ec: EC): Service[(Int, Int)] =
    Service.query( "select last_id, sum from sums order by last_id desc limit 1"
                 , { s =>
                       val r = s.executeQuery()
                       if (r.next()) {
                         (r.getInt("last_id"), r.getInt("sum"))
                       } else {
                         (0, 0)
                       }
                   }
                 )

  def update(implicit ec: EC): Service[Unit] =
    for {
      sum <- getSum
      amt <- getAmounts(sum._1)
      _   <- Service.update( "insert into sums (last_id, sum) values (?, ?)"
                           , { s =>
                                 s.setInt(1, amt._1)
                                 s.setInt(2, sum._2 + amt._2.sum)
                             }
                           )
      _   <- Service.update( "delete from sums where last_id < ?"
                           , _.setInt(1, amt._1)
                           )
    } yield ()
}
