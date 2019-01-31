scalaVersion := "2.12.5"
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

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"

enablePlugins(JettyPlugin)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")
