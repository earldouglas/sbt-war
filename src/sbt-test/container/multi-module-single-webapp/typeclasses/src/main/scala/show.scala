package typeclasses

trait Show {
  def show: String
}

object Show {

  implicit class ShowInt(x: Int) extends Show {
    lazy val show: String = x.toString
  }

}
