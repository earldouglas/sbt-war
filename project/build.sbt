// bintray for publishing
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

// scripted for plugin testing
libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value

// sbt-pgp for artifact signing
addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.1") 
