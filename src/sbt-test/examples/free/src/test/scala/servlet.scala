import java.net._
import org.scalatest._

object Http {

  def get(url: String): (Int, String) = {
    val c =
      new URL(url)
        .openConnection()
        .asInstanceOf[HttpURLConnection]
    c.setRequestMethod("GET")
    c.setDoInput(true)
    c.setDoOutput(false)
    val status = c.getResponseCode
    val responseStream =
      if (status < 400) c.getInputStream
      else c.getErrorStream
    val responseBody =
      scala.io.Source
        .fromInputStream(responseStream)
        .mkString
    c.disconnect()
    (status, responseBody)
  }
}

class ServletSuite extends FunSuite
                   with BeforeAndAfterAll
                   with Matchers
                   {

  private def awaitPort(port: Int, retries: Int = 40): Unit =
    try {
      val socket = new Socket()
      socket.connect(new InetSocketAddress("localhost", port))
      socket.close()
    } catch {
      case _: Exception =>
        if (retries > 0) {
          Thread.sleep(250)
          awaitPort(port, retries - 1)
        } else {
          throw new Exception(s"expected port $port to be open")
        }
    }

  override def beforeAll() {
    awaitPort(8080)
  }

  test("add a message") {
    Http
      .get("http://localhost:8080/")
      ._2 shouldBe
        "Missing required header: x-session-id"
  }
}
