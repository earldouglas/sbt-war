import akka.actor._
import spray.http._
import spray.http.HttpMethods._

class HelloWorldService extends Actor with ActorLogging {

  def receive = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      sender ! HttpResponse(entity = "Hello, world!")
  }

}
