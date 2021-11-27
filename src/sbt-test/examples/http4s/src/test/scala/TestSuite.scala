import org.scalatest._

class TestSuite extends FunSuite with Matchers {

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
