libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"

enablePlugins(TomcatPlugin)

containerArgs := Seq(
  "--enable-ssl"
)

javaOptions in Tomcat ++= Seq(
  "-Djavax.net.ssl.keyStore=keystore.jks",
  "-Djavax.net.ssl.keyStorePassword=changeit",
  "-Djavax.net.ssl.trustStore=cacerts.jks",
  "-Djavax.net.ssl.trustStorePassword=changeit"
)

containerPort := 8443
