import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AsyncServlet extends HttpServlet {

  val execSvc = java.util.concurrent.Executors.newFixedThreadPool(8)

  override def destroy(): Unit = {
    execSvc.shutdown
  }

  override def service(
      req: HttpServletRequest,
      res: HttpServletResponse
  ) {

    val ctx = req.startAsync

    execSvc submit {
      new Runnable() {
        override def run(): Unit = {

          res.setContentType("text/html")
          res.setCharacterEncoding("UTF-8")

          val responseBody: String =
            """<html>
              |  <body>
              |    <h1>Hello, world!</h1>
              |  </body>
              |</html>""".stripMargin
          res.getWriter.write(responseBody)

          ctx.complete
        }
      }
    }
  }
}
