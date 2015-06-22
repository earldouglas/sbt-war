addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

libraryDependencies <+= sbtVersion(v => "org.scala-sbt" % "scripted-plugin" % v)
