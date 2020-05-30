scalaVersion := "2.13.2"

libraryDependencies += "javax.servlet" %  "javax.servlet-api" % "3.1.0" % "provided"
libraryDependencies += "org.http4s"    %% "http4s-dsl"        % "0.21.4"
libraryDependencies += "org.http4s"    %% "http4s-servlet"    % "0.21.4"

enablePlugins(JettyPlugin)
