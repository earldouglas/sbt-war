scalaVersion := "2.13.3"
scalacOptions ++= Seq( "-deprecation"
                     , "-feature"
                     , "-unchecked"
                     , "-Ywarn-unused"
                     )

libraryDependencies += "com.h2database" % "h2" % "1.4.200"
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"

enablePlugins(JettyPlugin)

containerForkOptions :=
  ForkOptions().withEnvVars(
    Map( "DB_DRIVER" -> "org.h2.Driver"
       , "DB_URL" -> "jdbc:h2:mem:adder"
       , "DB_USER" -> "sa"
       , "DB_PASS" -> ""
       )
  )
