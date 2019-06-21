import java.util.UUID
import org.scalatra.AsyncResult
import org.scalatra.FutureSupport
import org.scalatra.ScalatraServlet
import org.scalatra.SweetCookies
import scala.collection.concurrent.TrieMap
import scala.collection.immutable.Map
import scala.concurrent.ExecutionContext
import scalaz.zio.DefaultRuntime
import scalaz.zio.Task

object OAuthedServlet {

  val secretStates: TrieMap[String, String] = TrieMap.empty
  val profiles: TrieMap[String, Profile] = TrieMap.empty

}

class OAuthedServlet
  extends ScalatraServlet
  with FutureSupport
  with DefaultRuntime {

  import OAuthedServlet._
  import MustacheSupport._
  import GoogleApiClient._

  implicit def executor: ExecutionContext =
    ExecutionContext.Implicits.global

  def async(x: Task[Any]): AsyncResult =
    new AsyncResult { val is =
      unsafeRun(x.toFuture)
    }

  def authn[A](cookies: SweetCookies)(k: String => Task[A]): AsyncResult =
    async {
      k {
        val sessionCookieName: String = "X-Session-ID"
        cookies.get(sessionCookieName) match {
          case Some(sessionId) => sessionId
          case None => 
            val sessionId: String = UUID.randomUUID().toString()
            cookies.set(sessionCookieName, sessionId)
            sessionId
        }
      }
    }

  get("/async") {
    async {
      Task {
        response.mustache("landing.mustache", Map.empty)
      }
    }
  }

  get("/") {
    authn(cookies) { sessionId =>
      contentType = "text/html; charset=utf-8"
      val model: Map[String, Any] =
        profiles.get(sessionId).map(p => ("profile" -> p)).toMap
      Task(response.mustache("landing.mustache", model))
    }
  }

  get("/google") {
    authn(cookies) { sessionId =>
      val secretState: String = UUID.randomUUID().toString()
      secretStates.put(sessionId, secretState)
      val authzUrl: String =
        google.createAuthorizationUrlBuilder()
              .state(secretState)
              .scope(List( "https://www.googleapis.com/auth/userinfo.email"
                         , "https://www.googleapis.com/auth/userinfo.profile"
                         ).mkString(" "))
              .build()
      Task(response.redirect(authzUrl))
    }
  }

  get("/google/callback") {
    authn(cookies) { sessionId =>
      Task {
        val state: String = params("state")
        (secretStates.get(sessionId).filter(_ == state)) map { _ =>
          secretStates.remove(sessionId)
          val code: String = params("code")
          google.getAccessToken(code)
        }
      } flatMap {
        case Some(accessToken) =>
          getProfile(accessToken) map { p =>
            profiles.put(sessionId, p)
          }
        case None =>
          Task(())
      } map { _ =>
        response.redirect("/")
      }
    }
  }

}
