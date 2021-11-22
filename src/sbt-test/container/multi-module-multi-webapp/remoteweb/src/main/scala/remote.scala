package remote

import java.net.URL
import java.net.HttpURLConnection
import java.util.Scanner

object `package` {

  def get(url: String): String =
    try {
      val conn =
        (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
      conn.setInstanceFollowRedirects(false)
      conn.setRequestMethod("GET")
      conn.setDoOutput(false)
      val s = new java.util.Scanner(conn.getInputStream)
        .useDelimiter("""\A""")
      val response = if (s.hasNext()) s.next() else ""
      conn.getInputStream.close
      response
    } catch {
      case e: Exception => ""
    }

}
