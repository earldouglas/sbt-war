import com.github.scribejava.apis.GoogleApi20
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.model.OAuthRequest
import com.github.scribejava.core.model.Response
import com.github.scribejava.core.model.Verb
import com.github.scribejava.core.oauth.OAuth20Service

object GoogleApiClient {

  val google: OAuth20Service =
    new ServiceBuilder(sys.env("GOOGLE_OAUTH_CLIENT_ID"))
      .apiSecret(sys.env("GOOGLE_OAUTH_CLIENT_SECRET"))
      .callback("https://oauthed.herokuapp.com/google/callback")
      .build(GoogleApi20.instance())

  def getProfile(accessToken: OAuth2AccessToken): Either[String,Profile] = {

    import argonaut._
    import argonaut.Argonaut._

    val request: OAuthRequest =
      new OAuthRequest( Verb.GET
                      , "https://www.googleapis.com/oauth2/v2/userinfo?fields=email%2Cname"
                      )
    google.signRequest(accessToken, request)
    val response: Response = google.execute(request)

    implicit val decodeProfile: DecodeJson[Profile] =
      DecodeJson {
        (c: HCursor) =>
          for {
            name  <- (c --\ "name").as[String]
            email <- (c --\ "email").as[String]
          } yield Profile( name = name
                         , email = email
                         )
      }
    response.getBody().decodeEither[Profile]
  }

}
