package local.test

import java.util

import javax.ws.rs.ApplicationPath
import javax.ws.rs.core.Application
import local.test.endpoint.Hello

@ApplicationPath("/*")
class Main extends Application {

  override def getClasses: util.Set[Class[_]] = {
    val h = new util.HashSet[Class[_]]
    h.add(classOf[Hello])
    h
  }
}
