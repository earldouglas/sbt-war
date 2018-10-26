scalaVersion := "2.12.5"
scalacOptions ++= Seq( "-deprecation"
                     , "-feature"
                     , "-unchecked"
                     , "-Ywarn-unused"
                     , "-Ywarn-unused-import"
                     )

libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.13"
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"

enablePlugins(JettyPlugin)

containerForkOptions :=
  ForkOptions().withEnvVars( Map( "DB_DRIVER" -> "com.mysql.cj.jdbc.Driver"
                                , "DB_URL"    -> "jdbc:mysql://localhost:3306/adder"
                                , "DB_USER"   -> "adder"
                                , "DB_PASS"   -> ""
                                )
                           )

containerScale := 5
