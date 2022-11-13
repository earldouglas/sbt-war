import akka.actor.ActorSystem
import akka.actor.Props
import spray.servlet.WebBoot

class Boot extends WebBoot {

  val system = ActorSystem("example")
  val serviceActor = system.actorOf(Props[HelloWorldService])

}
