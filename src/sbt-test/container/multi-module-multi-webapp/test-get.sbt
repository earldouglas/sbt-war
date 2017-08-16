val get = inputKey[Unit]("get")

get := {
  javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
    new javax.net.ssl.HostnameVerifier() {
      override def verify(hostname: String, sslSession: javax.net.ssl.SSLSession): Boolean = {
        hostname == "localhost"
      }
    }
  )
  complete.DefaultParsers.spaceDelimited("<arg>").parsed.toList match {
    case List(url, status) =>
      import java.net.URL
      import java.net.HttpURLConnection
      val conn = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
      conn.setInstanceFollowRedirects(false)
      conn.setRequestMethod("GET")
      conn.setDoOutput(false)
      Right(conn.getResponseCode)
      if (conn.getResponseCode != status.toInt)
        sys.error(s"Expected status $status but received ${conn.getResponseCode}")
    case _ => sys.error("Usage: get <url> <expected status>")
  }
}
