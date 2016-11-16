val findInManifest = InputKey[Unit]("find-in-manifest")

findInManifest := {
  val args: Seq[String] = Def.spaceDelimited("<arg>").parsed
  val zipFile = new java.util.zip.ZipFile(args(0))
  val manifestFilename = "META-INF/MANIFEST.MF"
  val manifestAttribute = args(1)
  Option(zipFile.getEntry(manifestFilename)) match {
    case Some(e) =>
      val is = zipFile.getInputStream(e)
      val manifestLines = scala.io.Source.fromInputStream(is).getLines()
      if (manifestLines.contains(manifestAttribute)) {
        ()
      } else {
        sys.error("Manifest " + manifestFilename + " is missing expected attribute " + manifestAttribute)
      }
    case None => sys.error("File " + manifestFilename + " not found in zip " + args(0))
  }
}
