scalaVersion := "2.12.8"

libraryDependencies += "javax.servlet"  %  "javax.servlet-api" % "3.1.0" % "provided"

libraryDependencies += "com.h2database" %  "h2"         % "1.4.194"
libraryDependencies += "org.scalaz"     %% "scalaz-zio" % "1.0-RC5"

enablePlugins(JettyPlugin)

containerForkOptions :=
  ForkOptions().withEnvVars {
    Map( "DB_DRIVER" -> "org.h2.Driver"
       , "DB_URL" -> "jdbc:h2:mem:zio"
       , "DB_USER" -> "sa"
       , "DB_PASS" -> ""
       )
  }
