import com.github.mustachejava.DefaultMustacheFactory
import com.twitter.mustache.ScalaObjectHandler
import java.io.File
import java.io.PrintWriter
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

object `package` {

  val mf = new DefaultMustacheFactory()
  mf.setObjectHandler(new ScalaObjectHandler)

  implicit class MustacheResonse(res: HttpServletResponse) {

    def render(templateName: String, model: Any): Unit = {

      val path: String =
        getClass.getClassLoader
          .getResource("greeting.mustache")
          .getFile

      val mustache = mf.compile(path)

      mustache.execute(res.getWriter, model)
    }

  }

}

class MustacheServlet extends HttpServlet {

  override def doGet(
      req: HttpServletRequest,
      res: HttpServletResponse
  ) {

    res.setContentType("text/html")
    res.setCharacterEncoding("UTF-8")

    res.render("greeting.mustache", Map("greeting" -> "Hello, world"))

  }

}
