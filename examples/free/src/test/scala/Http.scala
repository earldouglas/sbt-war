import java.net._
import scala.jdk.CollectionConverters._
import scala.io.Source

case class Response(
    status: Int,
    headers: Map[String, String],
    body: String
)

object Request {

  def apply(
      method: String,
      url: String,
      headers: Map[String, String],
      body: Option[String]
  ): Response = {

    val c =
      new URL(url)
        .openConnection()
        .asInstanceOf[HttpURLConnection]

    c.setInstanceFollowRedirects(false)
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
        headers = c
          .getHeaderFields()
          .asScala
          .filter({ case (k, _) => k != null })
          .map({ case (k, v) => (k, v.asScala.mkString(",")) })
          .toMap - "Date" - "Content-Length" - "Server",
        body = Source.fromInputStream {
          if (c.getResponseCode() < 400) {
            c.getInputStream
          } else {
            c.getErrorStream
          }
        }.mkString
      )

    c.disconnect()

    response
  }
}
