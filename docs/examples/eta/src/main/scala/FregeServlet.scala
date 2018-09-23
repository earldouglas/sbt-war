package fregeweb

import scala.language.implicitConversions

import javax.servlet.http.HttpServlet
import javax.servlet.http.{ HttpServletRequest => HSReq }
import javax.servlet.http.{ HttpServletResponse => HSRes }

import FregeWeb.TRequest
import FregeWeb.TResponse

import frege.run8.Box

class FregeServlet extends HttpServlet {

  override def service(hsReq: HSReq, hsRes: HSRes): Unit =
    hsRes service hsReq

}

object `package` {

  implicit class HSResService(val hsRes: HSRes) extends AnyVal {

    def service(hsReq: HSReq): Unit = {
      val tReq: TRequest = TRequest.mk( new Box(hsReq.method)
                                      , new Box(hsReq.uri)
                                      )
      val tRes: TResponse = FregeWeb.service(tReq).asInstanceOf[TResponse]
      write(tRes)
    }

    private def write(tRes: TResponse): Unit = {
      val status: Int = TResponse.status(tRes).asInstanceOf[Int]
      val body: String = TResponse.body(tRes).asInstanceOf[String]
      hsRes.setStatus(status)
      hsRes.getWriter().write(body)
    }

  }

  implicit class RichHSReq(val hsReq: HSReq) extends AnyVal {
    def method: String = hsReq.getMethod()
    def uri: String =
      if (hsReq.getRequestURI().startsWith(hsReq.getServletPath()))
        hsReq.getRequestURI().substring(hsReq.getServletPath().length())
      else
        hsReq.getRequestURI()
  }

}
