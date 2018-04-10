package code.snippet

import java.util.Date
import net.liftweb.util.Helpers._

class HelloWorld {

  def render = "* *" #> ((new Date()).toString)

}
