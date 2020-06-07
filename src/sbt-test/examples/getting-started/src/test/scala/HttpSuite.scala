import java.net._
import org.scalatest._
import scala.collection.JavaConverters._
import scala.io.Source

trait HttpSuite extends FunSuite with BeforeAndAfterAll {

  case class Response(
    status: Int,
    headers: Map[String, String],
    body: String
  )

  def request(
    method: String,
    url: String,
    headers: Map[String, String],
    body: Option[String]
  ): Response = {

    val c =
      new URL(url)
        .openConnection()
        .asInstanceOf[HttpURLConnection]

    c.setRequestMethod(method)
    c.setDoInput(true)
    c.setDoOutput(body.isDefined)

    headers foreach { case (k, v) =>
      c.setRequestProperty(k, v)
    }

    body foreach { b =>
      c.getOutputStream.write(b.getBytes("UTF-8"))
    }

    val response =
      Response(
        status = c.getResponseCode(),
        headers =
          c
            .getHeaderFields()
            .asScala
            .filter({ case (k, _) => k != null })
            .map({ case (k, v) => (k, v.asScala.mkString(",")) })
            .toMap - "Date" - "Content-Length" - "Server",
        body =
          Source
            .fromInputStream {
              if (c.getResponseCode() < 400) {
                c.getInputStream
              } else {
                c.getErrorStream
              }
            }
            .mkString
      )

    c.disconnect()

    response
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
}
