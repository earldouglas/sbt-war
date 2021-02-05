object HttpClient {

  import java.net.HttpURLConnection
  import java.net.URL
  import scala.collection.JavaConverters._
  import scala.io.Source
  import zio.Task
  import zio.ZIO

  case class Request( method: String
                    , url: URL
                    , headers: Map[String, String]
                    , body: Option[String]
                    )

  case class Response( status: Int
                     , headers: Map[String, String]
                     , body: String
                     )

  def request(r: Request): Task[Response] =
    ZIO effect {

      val c = r.url.openConnection().asInstanceOf[HttpURLConnection]

      c.setRequestMethod(r.method)
      c.setInstanceFollowRedirects(false)

      c.setDoInput(true)
      c.setDoOutput(r.body.isDefined)

      r.headers foreach { case (k, v) => c.setRequestProperty(k, v) }

      r.body foreach { b => c.getOutputStream.write(b.getBytes("UTF-8")) }

      val responseStatus = c.getResponseCode

      val responseHeaders =
        c.getHeaderFields.asScala.toMap map { case (k, v) =>
          (k, v.asScala.mkString(","))
        }

      val responseStream =
        if (responseStatus < 400) c.getInputStream
        else c.getErrorStream

      val responseBody =
        if (responseStream != null) {
          Source.fromInputStream(responseStream).mkString
        } else {
          ""
        }

      c.disconnect()

      Response( status = responseStatus
              , headers = responseHeaders
              , body = responseBody
              )
    }

  def post( url: URL
          , headers: Map[String, String] = Map.empty
          , body: Option[String] = None
          ): Task[Response] =
    request(Request("POST", url, headers, body))

  def get( url: URL
         , headers: Map[String, String] = Map.empty
         ): Task[Response] =
    request(Request("GET", url, headers, None))

  def put( url: URL
         , headers: Map[String, String] = Map.empty
         , body: Option[String] = None
         ): Task[Response] =
    request(Request("PUT", url, headers, body))

  def delete( url: URL
            , headers: Map[String, String] = Map.empty
            ): Task[Response] =
    request(Request("DELETE", url, headers, None))

  def head( url: URL
          , headers: Map[String, String]
          ): Task[Response] =
    request(Request("HEAD", url, headers, None))

  def options( url: URL
             , headers: Map[String, String]
             ): Task[Response] =
    request(Request("OPTIONS", url, headers, None))

}
