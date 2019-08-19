import java.net.URL
import com.earldouglas.zio.httpclient.HttpClient
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite
import zio.DefaultRuntime

class ServletSuite extends FunSuite
                   with BeforeAndAfterAll
                   with DefaultRuntime
                   {

  override def beforeAll() {
    AwaitPort(8080)
  }

  test("Add a message") {

    val res1 = unsafeRun {
      HttpClient.get(new URL("http://localhost:8080/"))
    }
    assert(res1.body === """|<ul>
                            |</ul>
                            |""".stripMargin)

    val res2 = unsafeRun {
      HttpClient.post(url = new URL("http://localhost:8080/?name=jdoe&message=Howdy!"))
    }
    assert(res2.headers.get("Location") === Some("http://localhost:8080/"))

    val res3 = unsafeRun {
      HttpClient.get(new URL("http://localhost:8080/"))
    }
    assert(res3.body === """|<ul>
                            |  <li>jdoe: Howdy!</li>
                            |</ul>
                            |""".stripMargin)
  }
}
