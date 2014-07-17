package runner

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.webapp.WebAppContext

object Run extends App {
 
  val port: Int = args(0).toInt
  val war: String = args(1)
  val contextPath: String = "/"

  val server = new Server()
  val connector = new ServerConnector(server)
  connector.setPort(port)
  server.addConnector(connector)

  val context = new WebAppContext()
  context.setContextPath(contextPath)
  context.setWar(war)
  server.setHandler(context)

  server.start
  server.join

}
