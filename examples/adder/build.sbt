libraryDependencies += "com.h2database" % "h2" % "2.2.222"
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.9" % "test"

enablePlugins(JettyPlugin)

containerForkOptions :=
  ForkOptions().withEnvVars(
    Map(
      "DB_DRIVER" -> "org.h2.Driver",
      "DB_URL" -> "jdbc:h2:mem:adder",
      "DB_USER" -> "sa",
      "DB_PASS" -> ""
    )
  )
