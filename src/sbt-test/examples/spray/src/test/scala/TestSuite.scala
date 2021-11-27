import org.scalatest._

class TestSuite extends FunSuite with Matchers {

  test("/") {
    Request("GET", "http://localhost:8080/", Map.empty, None) shouldBe
      Response(
        status = 200,
        Map("Content-Type" -> "text/plain;charset=utf-8"),
        "Hello, world!"
      )
  }
}
