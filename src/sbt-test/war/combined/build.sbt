name := "test"

scalaVersion := "2.13.3"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"

enablePlugins(WarPlugin)

webappPostProcess := {
  val s = streams.value.log
  webappDir =>
    def listFiles(level: Int)(f: File): Unit = {
      val indent = ((1 until level) map { _ => "  " }).mkString
      if (f.isDirectory) {
        s.info(indent + f.getName + "/")
        f.listFiles foreach { listFiles(level + 1) }
      } else s.info(indent + f.getName)
    }
    listFiles(1)(webappDir)
}
