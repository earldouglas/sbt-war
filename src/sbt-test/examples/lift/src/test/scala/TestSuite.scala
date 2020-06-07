import org.scalatest._

class TestSuite extends FunSuite with Matchers {

  test("/") {
    val response = Request("GET", "http://localhost:8080/", Map.empty, None)
    response.status shouldBe 200
    response.headers.get("Content-Type") shouldBe Some("text/html;charset=utf-8")
    response.body.contains("Hello, world!") shouldBe true
  }
}
