scalaVersion := "2.13.3"
scalacOptions ++= Seq( "-deprecation"
                     , "-feature"
                     , "-unchecked"
                     , "-Ywarn-unused"
                     , "-Ywarn-unused-import"
                     )

libraryDependencies += "com.h2database" % "h2" % "1.4.194"
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

enablePlugins(JettyPlugin)

containerForkOptions :=
  ForkOptions().withEnvVars(
    Map( "DB_DRIVER" -> "org.h2.Driver"
       , "DB_URL" -> "jdbc:h2:mem:adder"
       , "DB_USER" -> "sa"
       , "DB_PASS" -> ""
       )
  )
