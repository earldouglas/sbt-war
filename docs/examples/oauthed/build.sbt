scalaVersion := "2.12.5"

libraryDependencies += "com.github.scribejava"             %  "scribejava-apis"       % "6.6.3"
libraryDependencies += "com.github.spullara.mustache.java" %  "compiler"              % "0.9.5"
libraryDependencies += "com.github.spullara.mustache.java" %  "scala-extensions-2.12" % "0.9.5"
libraryDependencies += "org.scalatra"                      %% "scalatra"              % "2.5.0"
libraryDependencies += "io.argonaut"                       %% "argonaut"              % "6.2.2"

libraryDependencies += "javax.servlet"                     %  "javax.servlet-api"     % "3.1.0" % "provided"

webappWebInfClasses := true

enablePlugins(JettyPlugin)
enablePlugins(HerokuDeploy)

herokuAppName := "oauthed"

scalacOptions ++= Seq( "-deprecation"
                     , "-encoding", "utf8"
                     , "-feature"
                     , "-language:existentials"
                     , "-language:experimental.macros"
                     , "-language:higherKinds"
                     , "-language:implicitConversions"
                     , "-unchecked"
                     , "-Xfatal-warnings"
                     , "-Xlint"
                     , "-Ypartial-unification"
                     , "-Yrangepos"
                     , "-Ywarn-unused"
                     , "-Ywarn-unused-import"
                     )
