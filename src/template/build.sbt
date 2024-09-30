libraryDependencies += "org.typelevel" %% "cats-effect" % "3.5.4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"

libraryDependencies += "com.h2database" % "h2" % "2.2.224"
libraryDependencies += "jakarta.servlet" % "jakarta.servlet-api" % "6.0.0" % "provided"

scalaVersion := "3.5.0"
semanticdbEnabled := true
semanticdbVersion := scalafixSemanticdb.revision

scalacOptions += "-Wunused:all"
