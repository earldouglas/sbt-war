import com.github.scribejava.core.model.OAuth2AccessToken
import java.util.UUID
import org.scalatra.ScalatraServlet
import scala.collection.concurrent.TrieMap
import scala.collection.immutable.Map

object OAuthedServlet {
  val sessionCookieName: String = "X-Session-ID"
  val secretStates: TrieMap[String, String] = TrieMap.empty
  val profiles: TrieMap[String, Profile] = TrieMap.empty
}

class OAuthedServlet extends ScalatraServlet {

  import OAuthedServlet._
  import MustacheSupport._
  import GoogleApiClient._

  get("/") {
    contentType = "text/html; charset=utf-8"
    val model: Map[String, Any] =
      cookies.get(sessionCookieName) match {
        case Some(sessionId) =>
          profiles.get(sessionId).map(p => ("profile" -> p)).toMap
        case _ =>
          Map.empty
      }
    response.render("landing.mustache", model)
  }

  get("/google") {
    val sessionId: String = UUID.randomUUID().toString()
    val secretState: String = UUID.randomUUID().toString()
    secretStates.put(sessionId, secretState)
    val authzUrl: String =
      google.createAuthorizationUrlBuilder()
            .state(secretState)
            .scope(List( "https://www.googleapis.com/auth/userinfo.email"
                       , "https://www.googleapis.com/auth/userinfo.profile"
                       ).mkString(" "))
            .build()
    cookies.set(sessionCookieName, sessionId.toString())
    redirect(authzUrl)
  }

  get("/google/callback") {
    val state: String = params("state")
    val sessionIdO: Option[String] =
      for {
        sessionId <- cookies.get(sessionCookieName)
        savedState <- secretStates.get(sessionId)
        if (savedState == state)
      } yield sessionId
    sessionIdO match {
      case None => cookies.delete(sessionCookieName)
      case Some(sessionId) =>
        secretStates.remove(sessionId)
        val code: String = params("code")
        val accessToken: OAuth2AccessToken =
          google.getAccessToken(code)
        getProfile(accessToken).right.toOption foreach { p => 
          profiles.put(sessionId, p)
        }
    }
    redirect("/")
  }

}
