import org.eclipse.jetty.server._
import org.eclipse.jetty.servlet._
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

  val server = new Server(8080)

  override def beforeAll(): Unit = {
    val handler = new ServletHandler()
    server.setHandler(handler)
    handler.addServletWithMapping(classOf[AdderServlet], "/*")
    server.start()
  }

  override def afterAll(): Unit = {
    server.stop()
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
