import java.util.UUID

case class User(username: String)

sealed trait Permission
case object Read extends Permission
case object Write extends Permission
case object Delete extends Permission

sealed trait WebAppOp[+A]
case class Value[A](value: A) extends WebAppOp[A]
case object GetDatabase extends WebAppOp[Database]
case class RequestParam(name: String) extends WebAppOp[String]
case class RequestHeader(name: String) extends WebAppOp[String]
case class Error(e: WebAppErr) extends WebAppOp[Nothing]

sealed trait WebAppErr {
  def status: Int
  def message: String
}
case class InsufficientPermissions( required: Set[Permission]
                                  , permissions: Set[Permission]
                                  ) extends WebAppErr {
  val status: Int = 403
  val message: String = s"Insufficient permissions; required: ${required}, present: ${permissions}"
}
case class InvalidSession(sessionId: String) extends WebAppErr {
  val status: Int = 401
  val message: String = s"Invalid session: ${sessionId}"
}
case class InvalidUsernameOrPassword(username: String) extends WebAppErr {
  val status: Int = 404
  val message: String = s"Invalid username or password; username: ${username}"
}
case class MissingParameter(name: String) extends WebAppErr {
  val status: Int = 400
  val message: String = s"Missing required parameter: ${name}"
}
case class MissingHeader(name: String) extends WebAppErr {
  val status: Int = 400
  val message: String = s"Missing required header: ${name}"
}
case class InternalServerError(throwable: Throwable) extends WebAppErr {
  val status: Int = 500
  val message: String = throwable.getMessage
}

object WebAppOp {

  private val permissions: Map[String, Set[Permission]] =
    Map( "tbuckland" -> Set(Read, Write)
       , "lbracco"   -> Set(Write)
       , "jdoe"      -> Set(Read)
       )

  private val passwordsByUsername: Map[String, String] =
    Map( "tbuckland" -> "alligator3"
       , "lbracco"   -> "secret"
       , "jdoe"      -> "unguessable"
       )

  private var sessions: Map[String, User] =
    Map.empty

  def signIn: Free[WebAppOp, String] =
    for {
      username  <- Free.liftM(RequestParam("username"))
      password  <- Free.liftM(RequestParam("password"))
      sessionId <- Free.liftM {
                     if (passwordsByUsername.get(username) == Some(password)) {
                       val sessionId = UUID.randomUUID.toString
                       val user = User(username)
                       sessions = sessions + (sessionId -> user)
                       Value(sessionId)
                     } else {
                       Error(InvalidUsernameOrPassword(username))
                     }
                   }
    } yield sessionId

  def authN: Free[WebAppOp, User] =
    for {
      sessionId <- Free.liftM(RequestHeader("x-session-id"))
      user      <- Free.liftM {
                     sessions.get(sessionId) match {
                       case Some(user) => Value(user)
                       case None => Error(InvalidSession(sessionId))
                     }
                   }
    } yield user

  def authZ[A]( required: Set[Permission]
              , k: => A
              ): Free[WebAppOp, A] =
    for {
      perms  <- getPermissions
      result <- Free.liftM {
                  if ((required -- perms).isEmpty) Value(k)
                  else Error(InsufficientPermissions(required, perms))
                }
    } yield result

  val getPermissions: Free[WebAppOp, Set[Permission]] =
    for {
      user <- WebAppOp.authN
    } yield permissions.getOrElse(user.username, Set.empty)

  val getSecret: Free[WebAppOp, String] =
    authZ(Set(Read), "The duck flies at midnight.")
}
