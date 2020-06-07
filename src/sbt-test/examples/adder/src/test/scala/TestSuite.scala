import org.scalatest._

class TestSuite extends FunSuite with Matchers {

  test("get sum") {
    Request("GET", "http://localhost:8080/", Map.empty, None) shouldBe
      Response(status = 200, Map("Content-Type" -> "text/plain;charset=utf-8"), "0\n")
  }

  test("add some numbers") {
    Request("POST", "http://localhost:8080/", Map.empty, Some("1")) shouldBe 
      Response(status = 201, Map.empty, "")

    Request("POST", "http://localhost:8080/", Map.empty, Some("11")) shouldBe 
      Response(status = 201, Map.empty, "")

    Request("POST", "http://localhost:8080/", Map.empty, Some("13")) shouldBe 
      Response(status = 201, Map.empty, "")

    Request("POST", "http://localhost:8080/", Map.empty, Some("17")) shouldBe 
      Response(status = 201, Map.empty, "")

    Thread.sleep(300)

    Request("GET", "http://localhost:8080/", Map.empty, None) shouldBe
      Response(status = 200, Map("Content-Type" -> "text/plain;charset=utf-8"), "42\n")
  }

  test("post an unparseable number") {
    Request("POST", "http://localhost:8080/", Map.empty, Some("forty-two")) shouldBe
      Response(status = 400, Map("Content-Type" -> "text/plain;charset=iso-8859-1"), "couldn't parse number")
  }
}
