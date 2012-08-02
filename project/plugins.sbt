libraryDependencies <+= (sbtVersion) { sv => sv match {
    case "0.12.0" => "org.scala-sbt" % "scripted-plugin" % sv
    case "0.11.3" => "org.scala-sbt" %% "scripted-plugin" % sv
    case "0.11.2" => "org.scala-tools.sbt" % "scripted-plugin" % sv
    case _ => error("Not supported")
  }
}

addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.5-SNAPSHOT")
