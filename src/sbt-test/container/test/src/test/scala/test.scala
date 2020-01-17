package krud

import java.net.HttpURLConnection
import java.net.URL
import org.scalatest.FunSuite
import org.scalatest.Matchers

class TestServletSuite extends FunSuite
                       with Matchers {

  def getStatus(url: String): Int = {
    val c = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    c.setRequestMethod("GET")
    c.setDoInput(true)
    c.setDoOutput(false)
    val status = c.getResponseCode
    c.getInputStream()
    status
  }

  test("GET /test") {
    getStatus("http://localhost:8080/test") shouldBe 200
  }
}
