val get = inputKey[Unit]("get")

get := baseDirectory { d =>
  System.setProperty("javax.net.ssl.trustStore", (d / "etc" / "keystore").getPath)
  System.setProperty("javax.net.ssl.trustStorePassword", "storepwd")
  javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
    new javax.net.ssl.HostnameVerifier() {
      def verify(hostname: String, sslSession: javax.net.ssl.SSLSession) =
        hostname.equals("localhost")
    }
  )
  def get(url: String, expect: Int, retries: Int): Unit = {
    val status: Either[Exception,Int] =
      try {
        import java.net.URL
        import java.net.HttpURLConnection
        val conn = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
        conn.setInstanceFollowRedirects(false)
        conn.setRequestMethod("GET")
        conn.setDoOutput(false)
        Right(conn.getResponseCode)
      } catch {
        case e: Exception => Left(e)
      }
    status match {
      case Right(s) if s == expect => ()
      case Right(s) =>
        sys.error("Expected status " + expect + " but received " + s)
      case Left(e) if retries > 0 =>
        Thread.sleep(2000)
        get(url, expect, retries - 1)
      case Left(e) =>
        sys.error("Caught exception " + e.toString)
    }
  }
  val args: Seq[String] = complete.DefaultParsers.spaceDelimited("<arg>").parsed
  get(args(0), args(1).toInt, 10)
}
