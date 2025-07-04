enablePlugins(SbtWar)

warPort := 8082

////////////////////////////////////////////////////////////////////////

TaskKey[Unit]("await-open") := {

  def isOpen(port: Int): Boolean =
    try {
      import java.net.Socket
      import java.net.InetSocketAddress
      val socket: Socket = new Socket()
      socket.connect(new InetSocketAddress("localhost", port))
      socket.close()
      true
    } catch {
      case e: Exception => false
    }

  def awaitOpen(port: Int, retries: Int = 40): Unit =
    if (!isOpen(port)) {
      if (retries > 0) {
        Thread.sleep(250)
        awaitOpen(port, retries - 1)
      } else {
        throw new Exception(s"expected port $port to be open")
      }
    }

  awaitOpen(warPort.value)
}

TaskKey[Unit]("await-closed") := {

  def isOpen(port: Int): Boolean =
    try {
      import java.net.Socket
      import java.net.InetSocketAddress
      val socket: Socket = new Socket()
      socket.connect(new InetSocketAddress("localhost", port))
      socket.close()
      true
    } catch {
      case e: Exception => false
    }

  def awaitClosed(port: Int, retries: Int = 40): Unit =
    if (isOpen(port)) {
      if (retries > 0) {
        Thread.sleep(250)
        awaitClosed(port, retries - 1)
      } else {
        throw new Exception(s"expected port $port to be closed")
      }
    }

  awaitClosed(warPort.value)
}

def check(
    log: sbt.internal.util.ManagedLogger,
    warPort: Int,
    path: String,
    expectedStatus: Int,
    expectedBody: Option[String]
): Unit = {

  import java.net.HttpURLConnection
  import java.net.URI
  import java.net.URL
  import javax.net.ssl.HostnameVerifier
  import javax.net.ssl.HttpsURLConnection
  import javax.net.ssl.SSLSession
  import scala.io.Source

  HttpsURLConnection.setDefaultHostnameVerifier(
    new HostnameVerifier() {
      override def verify(
          hostname: String,
          sslSession: SSLSession
      ): Boolean = {
        hostname == "localhost"
      }
    }
  )

  val url: String = s"http://localhost:${warPort}${path}"

  val c: HttpURLConnection =
    (new URI(url)
      .toURL())
      .openConnection
      .asInstanceOf[HttpURLConnection]

  val name: String = s"SbtWar: GET ${url}"

  c.setInstanceFollowRedirects(false)
  c.setRequestMethod("GET")
  c.setDoOutput(false)

  val obtainedStatus: Int =
    c.getResponseCode()
  val obtainedBody: String =
    try {
      Source.fromInputStream(c.getInputStream()).mkString
    } catch {
      case e: Exception =>
        Source.fromInputStream(c.getErrorStream()).mkString
    }

  val statusMatch: Boolean =
    expectedStatus == obtainedStatus
  val bodyMatch: Boolean =
    expectedBody
      .map(_ == obtainedBody)
      .getOrElse(true)

  if (!statusMatch || !bodyMatch) {
    log.error(name)
    sys.error(
      s"""|${name}:
          |  expected:
          |    * status: ${expectedStatus}
          |    * body:
          |      > ${expectedBody
           .toString()
           .replaceAll("\n", "\n      > ")}
          |  obtained:
          |    * status: ${obtainedStatus}
          |    * body:
          |      > ${obtainedBody
           .toString()
           .replaceAll("\n", "\n      > ")}
          |""".stripMargin
    )
  } else {
    log.success(name)
  }

}

TaskKey[Unit]("check-test-200") := {

  check(
    log = streams.value.log,
    warPort = warPort.value,
    path = "/test",
    expectedStatus = 200,
    expectedBody = Some("""<h1>Testing!</h1>""")
  )

}

TaskKey[Unit]("check-test-404") := {

  check(
    log = streams.value.log,
    warPort = warPort.value,
    path = "/test",
    expectedStatus = 404,
    expectedBody = None
  )

}
