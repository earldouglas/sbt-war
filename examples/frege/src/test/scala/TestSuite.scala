import org.scalatest._

class TestSuite extends FunSuite with Matchers {

  test("/") {
    Request(
      "GET",
      "http://localhost:8080/",
      Map.empty,
      body = None
    ) shouldBe
      Response(200, Map.empty, """<a href="hello">hello</a>""")
  }

  test("/hello") {
    Request(
      "GET",
      "http://localhost:8080/hello",
      Map.empty,
      None
    ) shouldBe
      Response(200, Map.empty, "Hello, world!")
  }

  test("/foo") {
    Request(
      "GET",
      "http://localhost:8080/foo",
      Map.empty,
      None
    ) shouldBe
      Response(404, Map.empty, "404'd!")
  }
}
