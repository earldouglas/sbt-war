import java.security.cert._
import javax.net.ssl._
import org.scalatest._
import java.security.SecureRandom

class TestSuite extends FunSuite with BeforeAndAfterAll with Matchers {

  override def beforeAll(): Unit = {
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(
      null,
      Array(
        new X509TrustManager {
          val getAcceptedIssuers = null
          def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String) = {}
          def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String) = {}
        }
      ),
      new SecureRandom()
    )

    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory)
    HttpsURLConnection.setDefaultHostnameVerifier(
      new HostnameVerifier {
        def verify(s: String, sslSession: SSLSession) = true
      }
    )
  }

  test("/") {
    Request("GET", "https://localhost:8443/", Map.empty, None) shouldBe
      Response(
        200,
        Map(
          "Keep-Alive" -> "timeout=60",
          "Connection" -> "keep-alive",
          "Content-Type" -> "text/html;charset=UTF-8"
        ),
        """<html>
          |  <body>
          |    <h1>Hello, world!</h1>
          |  </body>
          |</html>""".stripMargin
      )
  }
}
