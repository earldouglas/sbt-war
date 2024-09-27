enablePlugins(WarPackageRunnerPlugin)

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

TaskKey[Unit]("check") := {

  import java.net.HttpURLConnection
  import java.net.URI
  import java.net.URL
  import javax.net.ssl.HostnameVerifier
  import javax.net.ssl.HttpsURLConnection
  import javax.net.ssl.SSLSession
  import scala.io.Source

  val log: sbt.internal.util.ManagedLogger = streams.value.log

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

  def assertEquals(
      url: String,
      expectedBody: String
  ): Unit = {
    try {
      val c: HttpURLConnection =
        (new URI(url)
          .toURL())
          .openConnection
          .asInstanceOf[HttpURLConnection]

      val name: String = s"WarRunnerPlugin: GET ${url}"

      c.setInstanceFollowRedirects(false)
      c.setRequestMethod("GET")
      c.setDoOutput(false)

      val obtainedStatus: Int =
        c.getResponseCode()
      val obtainedBody: String =
        Source.fromInputStream(c.getInputStream()).mkString

      val expectedStatus: Int = 200

      val statusMatch: Boolean =
        expectedStatus == obtainedStatus
      val bodyMatch: Boolean =
        expectedBody == obtainedBody

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
    } catch {
      case e: Exception => log.error(s"${name}: ${e}")
    }
  }

  assertEquals(
    url = s"http://localhost:${warPort.value}/",
    expectedBody = Source
      .fromFile((Compile / sourceDirectory).value / "webapp" / "index.html")
      .mkString
  )

  assertEquals(
    url = s"http://localhost:${warPort.value}/count",
    expectedBody = """|{
                      |  "count": 1
                      |}
                      |""".stripMargin
  )

  assertEquals(
    url = s"http://localhost:${warPort.value}/count",
    expectedBody = """|{
                      |  "count": 2
                      |}
                      |""".stripMargin
  )

  assertEquals(
    url = s"http://localhost:${warPort.value}/count",
    expectedBody = """|{
                      |  "count": 3
                      |}
                      |""".stripMargin
  )

  assertEquals(
    url = s"http://localhost:${warPort.value}/count",
    expectedBody = """|{
                      |  "count": 4
                      |}
                      |""".stripMargin
  )

  assertEquals(
    url = s"http://localhost:${warPort.value}/hello",
    expectedBody = """<h1>Hello, world!</h1>"""
  )
}
