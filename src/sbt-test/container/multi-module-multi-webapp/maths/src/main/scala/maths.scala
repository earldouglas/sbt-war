package maths

object `package` {

  def factorial(x: Int): Int =
    (1 to x).foldLeft(1)((x, y) => x * y)

}
