scalaVersion := "2.12.5"
scalacOptions ++= Seq( "-deprecation"
                     , "-feature"
                     , "-unchecked"
                     , "-Ywarn-unused"
                     , "-Ywarn-unused-import"
                     )

libraryDependencies += "mysql"             %  "mysql-connector-java" % "8.0.13"
libraryDependencies += "javax.servlet"     %  "javax.servlet-api"    % "3.1.0"           % "provided"
libraryDependencies += "com.h2database"    %  "h2"                   % "1.4.194"         % "test"
libraryDependencies += "org.scalatest"     %% "scalatest"            % "3.0.5"           % "test"
libraryDependencies += "org.eclipse.jetty" % "jetty-runner"          % "9.4.8.v20171121" % "test"


enablePlugins(JettyPlugin)
containerScale := 5
containerForkOptions :=
  ForkOptions().withEnvVars( Map( "DB_DRIVER" -> "com.mysql.cj.jdbc.Driver"
                                , "DB_URL"    -> "jdbc:mysql://localhost:3306/adder"
                                , "DB_USER"   -> "adder"
                                , "DB_PASS"   -> ""
                                )
                           )

fork in Test:= true
envVars in Runtime := Map( "DB_DRIVER" -> "org.h2.Driver"
                         , "DB_URL" -> "jdbc:h2:mem:adder"
                         , "DB_USER" -> "sa"
                         , "DB_PASS" -> ""
                         )
