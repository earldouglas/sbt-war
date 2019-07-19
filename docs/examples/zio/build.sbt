scalaVersion := "2.12.8"
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

resolvers += Resolver.bintrayRepo("earldouglas", "maven")

libraryDependencies += "dev.zio"         %% "zio"      % "1.0.0-RC10-1"
libraryDependencies += "com.earldouglas" %% "zio-jdbc" % "1.0.0"

libraryDependencies += "javax.servlet"  %  "javax.servlet-api" % "3.1.0" % "provided"

libraryDependencies += "com.h2database" %  "h2"         % "1.4.194"
libraryDependencies += "com.jolbox" % "bonecp" % "0.8.0.RELEASE"

enablePlugins(JettyPlugin)

containerForkOptions :=
  ForkOptions().withEnvVars {
    Map( "DB_DRIVER" -> "org.h2.Driver"
       , "DB_URL" -> "jdbc:h2:mem:zio"
       , "DB_USER" -> "sa"
       , "DB_PASS" -> ""
       )
  }
