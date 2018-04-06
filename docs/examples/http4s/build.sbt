scalaVersion := "2.12.5"

libraryDependencies += "javax.servlet" %  "javax.servlet-api" % "3.1.0" % "provided"
libraryDependencies += "org.http4s"    %% "http4s-dsl"        % "0.18.7"
libraryDependencies += "org.http4s"    %% "http4s-servlet"    % "0.18.7"

enablePlugins(JettyPlugin)
