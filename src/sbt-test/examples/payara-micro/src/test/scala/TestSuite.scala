import org.scalatest._

class TestSuite extends FunSuite with BeforeAndAfterAll with Matchers {

  def waitForServer(retries: Int = 30): Unit =
    Request("GET", "http://localhost:8080/hello", Map.empty, None).status match {
      case 404 if retries > 0 => Thread.sleep(1000) ; waitForServer(retries - 1)
      case _ => ()
    }

  override def beforeAll(): Unit = {
    waitForServer()
  }

  test("hello") {
    Request("GET", "http://localhost:8080/hello?name=James", Map.empty, None) shouldBe
      Response(status = 200, Map("Content-Type" -> "text/plain;charset=utf-8"), "Hallo James\n")
  }
}
