import java.net._
import org.scalatest._
import scala.collection.JavaConverters._

class ServletSuite extends FunSuite with BeforeAndAfterAll with Matchers {

  case class Response(status: Int, headers: Map[String, String], body: String)

  def request(method: String, url: String, headers: Map[String, String], body: Option[String]): Response = {
    val c = new URL(url).openConnection().asInstanceOf[HttpURLConnection]
    c.setRequestMethod(method)
    c.setDoInput(true)
    c.setDoOutput(body.isDefined)
    headers foreach { case (k, v) => c.setRequestProperty(k, v) }
    body foreach { b => c.getOutputStream.write(b.getBytes("UTF-8")) }
    val status = c.getResponseCode
    val responseStream = if (status < 400) c.getInputStream else c.getErrorStream
    val responseHeaders = c.getHeaderFields().asScala.filter({ case (k, _) => k != null }).map({ case (k, v) => (k, v.asScala.mkString(";")) }).toMap
    val responseBody = scala.io.Source.fromInputStream(responseStream).mkString
    c.disconnect()
    Response(status, responseHeaders - "Date" - "Content-Length" - "Server", responseBody)
  }

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

  test("/") {
    request(
      method = "GET",
      url = "http://localhost:8080/",
      headers = Map.empty,
      body = None
    ) shouldBe {
      Response(
        status = 200,
        headers = Map.empty,
        body = """<a href="hello">hello</a>"""
      )
    }
  }

  test("/hello") {
    request(
      method = "GET",
      url = "http://localhost:8080/hello",
      headers = Map.empty,
      body = None
    ) shouldBe {
      Response(
        status = 200,
        headers = Map.empty,
        body = "Hello, world!"
      )
    }
  }

  test("/foo") {
    request(
      method = "GET",
      url = "http://localhost:8080/foo",
      headers = Map.empty,
      body = None
    ) shouldBe {
      Response(
        status = 404,
        headers = Map.empty,
        body = "404'd!"
      )
    }
  }
}
