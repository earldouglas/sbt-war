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

val get = inputKey[Unit]("get")

get := {
  complete.DefaultParsers.spaceDelimited("<arg>").parsed.toList match {
    case List(url, status) =>
      get(url, status.toInt, 10)
    case List(url, status, retries) =>
      get(url, status.toInt, retries.toInt)
    case _ =>
      throw new Exception("Usage: get <url> <expected status> [retries]")
  }
}
