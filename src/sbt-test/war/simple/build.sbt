name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

warSettings

webappPostProcess := {
  webappDir =>
    def listFiles(level: Int)(f: File): Unit = {
      val indent = ((1 until level) map { _ => "  " }).mkString
      if (f.isDirectory) {
        streams.value.log.info(indent + f.getName + "/")
        f.listFiles foreach { listFiles(level + 1) }
      } else streams.value.log.info(indent + f.getName)
    }
    listFiles(1)(webappDir)
}
