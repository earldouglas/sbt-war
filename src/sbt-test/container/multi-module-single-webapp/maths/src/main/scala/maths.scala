package maths

object `package` {

  def factorial(x: Int): Int =
    numbers.oneto(x).foldLeft(1)((x, y) => x * y)

}
