import org.scalatest._

class TestSuite extends FunSuite with Matchers {

  test("no messages yet") {
    Request(
      method = "GET",
      url = "http://localhost:8080/",
      headers = Map.empty,
      body = None
    ) shouldBe {
      Response(
        status = 200,
        headers = Map("Content-Type" -> "text/html;charset=utf-8"),
        body = 
          """|<ul>
             |</ul>
             |""".stripMargin
      )
    }
  }

  test("add a message") {
    Request(
      method = "POST",
      url = "http://localhost:8080/",
      headers = Map.empty,
      body = Some("name=jdoe&message=Howdy!"),
    ) shouldBe {
      Response(
        status = 302,
        headers = Map("Location" -> "http://localhost:8080/"),
        body = ""
      )
    }
  }

  test("retrieve the message") {
    Request(
      method = "GET",
      url = "http://localhost:8080/",
      headers = Map.empty,
      body = None
    ) shouldBe {
      Response(
        status = 200,
        headers = Map("Content-Type" -> "text/html;charset=utf-8"),
        body = 
          """|<ul>
             |  <li>jdoe: Howdy!</li>
             |</ul>
             |""".stripMargin
      )
    }
  }
}
