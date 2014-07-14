name := "test"

version := "0.1.0-SNAPSHOT"

webappSettings

unmanagedResourceDirectories in Compile <+= (sourceDirectory in Compile)(_ / "extra")

postProcess in webapp := {
  webappDir =>
    val fooHtml = new java.io.File(webappDir, "foo.html")
    val writer = new java.io.FileWriter(fooHtml)
    writer.write("""<html><body>foo</body></html>""")
    writer.close
}
