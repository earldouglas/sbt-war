class Database {

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

  def getPermissions(username: String): Set[Permission] =
    permissions.getOrElse(username, Set.empty)

  def findUsernameAndPassword(username: String, password: String): Boolean =
    passwordsByUsername.get(username) == Some(password)

  def addSession(sessionId: String, user: User): Unit =
    sessions = sessions + (sessionId -> user)
}
