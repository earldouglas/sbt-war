import java.net.InetSocketAddress
import java.net.Socket
import org.scalatest._
import scala.io.Source

object Http {

  import java.net._

  def get(url: String): (Int, String) = {
    val c = new URL(url).openConnection().asInstanceOf[HttpURLConnection]
    c.setRequestMethod("GET")
    c.setDoInput(true)
    c.setDoOutput(false)
    val status = c.getResponseCode
    val responseStream =
      if (status < 400) c.getInputStream
      else c.getErrorStream
    val responseBody =
      Source.fromInputStream(responseStream).mkString
    c.disconnect()
    (status, responseBody)
  }

  def post(url: String, body: String): (Int, String) = {
    val c = new URL(url).openConnection().asInstanceOf[HttpURLConnection]
    c.setRequestMethod("GET")
    c.setDoInput(true)
    c.setDoOutput(true)
    c.getOutputStream.write(body.getBytes("UTF-8"))
    val status = c.getResponseCode
    val responseStream =
      if (status < 400) c.getInputStream
      else c.getErrorStream
    val responseBody =
      Source.fromInputStream(responseStream).mkString
    c.disconnect()
    (status, responseBody)
  }

}

class Suite extends FunSuite with BeforeAndAfterAll with Matchers {

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

  override def beforeAll(): Unit = {
    awaitPort(8080)
  }

  test("get sum") {
    Http.get("http://localhost:8080/") shouldBe (200, "0\n")
  }

  test("add some numbers") {
    Http.post("http://localhost:8080/",  "1") shouldBe (201, "")
    Http.post("http://localhost:8080/", "11") shouldBe (201, "")
    Http.post("http://localhost:8080/", "13") shouldBe (201, "")
    Http.post("http://localhost:8080/", "17") shouldBe (201, "")
    Thread.sleep(300)
    Http.get("http://localhost:8080/") shouldBe (200, "42\n")
  }

  test("post an unparseable number") {
    Http.post("http://localhost:8080/", "forty-two") shouldBe (400, "couldn't parse number")
  }
}
