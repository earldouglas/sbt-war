enablePlugins(WarPackagePlugin)

////////////////////////////////////////////////////////////////////////

InputKey[Unit]("checkManifest") := {

  import java.io.InputStream
  import java.util.zip.ZipFile
  import scala.io.Source

  val zipFile: ZipFile = new ZipFile(warFile.value)

  val args: Seq[String] = Def.spaceDelimited("<arg>").parsed

  val rule: String = args(0)
  val manifestAttribute: String = args(1)

  val manifestFilename: String = "META-INF/MANIFEST.MF"

  Option(zipFile.getEntry(manifestFilename)) match {
    case Some(e) =>
      val is: InputStream = zipFile.getInputStream(e)
      val manifestLines: Seq[String] =
        Source.fromInputStream(is).getLines().toSeq

      (rule, manifestLines.contains(manifestAttribute)) match {
        case ("includes", true) =>
          ()
        case ("includes", false) =>
          sys.error(
            "Manifest " +
              manifestFilename +
              " is missing expected attribute " +
              manifestAttribute
          )
        case ("excludes", true) =>
          sys.error(
            "Manifest " +
              manifestFilename +
              " contains unexpected attribute " +
              manifestAttribute
          )
        case ("excludes", false) =>
          ()
        case _ =>
          sys.error(
            "Invalid rule " +
              rule
          )
      }
    case None =>
      sys.error(
        "File " +
          manifestFilename +
          " not found in zip " +
          zipFile
      )
  }
}
