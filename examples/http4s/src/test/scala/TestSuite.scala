import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class TestSuite extends AnyFunSuite with Matchers {

  test("hello") {
    Request(
      "GET",
      "http://localhost:8080/hello/James",
      Map.empty,
      None
    ) shouldBe
      Response(
        status = 200,
        Map("Content-Type" -> "text/plain;charset=utf-8"),
        "Hello, James."
      )
  }
}
