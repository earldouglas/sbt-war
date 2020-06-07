import org.scalatest._

class TestSuite extends FunSuite with Matchers {

  test("require session") {
    Request("GET", "http://localhost:8080/", Map.empty, None) shouldBe
      Response(400, Map.empty, "Missing required header: x-session-id")
  }

  test("require valid session") {
    Request("GET", "http://localhost:8080/", Map("X-Session-ID" -> "foo123"), None) shouldBe
      Response(401, Map.empty, "Invalid session: foo123")
  }

  test("sign in") {
    val sessionId =
      Request("POST", "http://localhost:8080/", Map.empty, Some("username=tbuckland&password=alligator3"))
        .headers
        .get("X-Session-ID")
        .get

    Request("GET", "http://localhost:8080/", Map("X-Session-ID" -> sessionId), None) shouldBe
      Response(200, Map.empty, "The duck flies at midnight.")
  }
}
