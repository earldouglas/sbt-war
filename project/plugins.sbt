libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.4")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")
addSbtPlugin("com.github.sbt" % "sbt-java-formatter" % "0.10.0")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.12.2")
