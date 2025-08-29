enablePlugins(SbtWar)

warPort := 8083

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
        sys.error(s"expected port $port to be open")
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
        sys.error(s"expected port $port to be closed")
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

  if (!statusMatch && !bodyMatch) {
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
           .replaceAll("\n", "\n      > ")}""".stripMargin
    )
  } else if (!statusMatch) {
    sys.error(
      s"""|${name}:
          |  expected status: ${expectedStatus}
          |  obtained status: ${obtainedStatus}""".stripMargin
    )
  } else if (!bodyMatch) {
    sys.error(
      s"""|${name}:
          |  expected body:
          |      > ${expectedBody
           .toString()
           .replaceAll("\n", "\n      > ")}
          |  obtained body:
          |      > ${obtainedBody
           .toString()
           .replaceAll("\n", "\n      > ")}""".stripMargin
    )
  } else {
    log.success(
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
           .replaceAll("\n", "\n      > ")}""".stripMargin
    )
  }

}

TaskKey[Unit]("check") := {

  import scala.io.Source

  check(
    log = streams.value.log,
    warPort = warPort.value,
    path = "/",
    expectedStatus = 200,
    expectedBody = Some(
      Source
        .fromFile("src/main/webapp/index.html")
        .mkString
    )
  )

  check(
    log = streams.value.log,
    warPort = warPort.value,
    path = "/count",
    expectedStatus = 200,
    expectedBody = Some(
      """|{
         |  "count": 1
         |}
         |""".stripMargin
    )
  )

  check(
    log = streams.value.log,
    warPort = warPort.value,
    path = "/count",
    expectedStatus = 200,
    expectedBody = Some(
      """|{
         |  "count": 2
         |}
         |""".stripMargin
    )
  )

  check(
    log = streams.value.log,
    warPort = warPort.value,
    path = "/count",
    expectedStatus = 200,
    expectedBody = Some(
      """|{
         |  "count": 3
         |}
         |""".stripMargin
    )
  )

  check(
    log = streams.value.log,
    warPort = warPort.value,
    path = "/count",
    expectedStatus = 200,
    expectedBody = Some(
      """|{
         |  "count": 4
         |}
         |""".stripMargin
    )
  )

  check(
    log = streams.value.log,
    warPort = warPort.value,
    path = "/hello",
    expectedStatus = 200,
    expectedBody = Some("""<h1>Hello, world!</h1>""")
  )

}
