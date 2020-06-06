import com.earldouglas.zio.httpclient.HttpClient
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite
import org.scalatest.Matchers
import zio.DefaultRuntime

class ServletSuite extends FunSuite
                   with BeforeAndAfterAll
                   with Matchers
                   with DefaultRuntime
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

    val res1 = unsafeRun {
      HttpClient.get(new URL("http://localhost:8080/"))
    }
    res1.body shouldBe """|<ul>
                          |</ul>
                          |""".stripMargin

    val res2 = unsafeRun {
      HttpClient.post(url = new URL("http://localhost:8080/?name=jdoe&message=Howdy!"))
    }
    res2.headers.get("Location") shouldBe Some("http://localhost:8080/")

    val res3 = unsafeRun {
      HttpClient.get(new URL("http://localhost:8080/"))
    }
    res3.body shouldBe """|<ul>
                          |  <li>jdoe: Howdy!</li>
                          |</ul>
                          |""".stripMargin
  }
}
