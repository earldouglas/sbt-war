import com.github.mustachejava.DefaultMustacheFactory
import com.twitter.mustache.ScalaObjectHandler
import javax.servlet.http.HttpServletResponse

object MustacheSupport {

  private val mf = {
    val mf: DefaultMustacheFactory = new DefaultMustacheFactory()
    mf.setObjectHandler(new ScalaObjectHandler)
    mf
  }

  implicit class MustacheResonse(res: HttpServletResponse) {
    def mustache( templateName: String
                , model: Any
                ): Unit = {
      val path: String =
        getClass.getClassLoader
                .getResource(templateName)
                .getFile
      val mustache = mf.compile(path)
      mustache.execute(res.getWriter, model)
    }
  }

}
