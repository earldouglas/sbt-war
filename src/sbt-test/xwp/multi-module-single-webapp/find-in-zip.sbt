val findInZip = InputKey[Unit]("find-in-zip")

findInZip := {
  val args: Seq[String] = Def.spaceDelimited("<arg>").parsed
  val zipFile = new java.util.zip.ZipFile(args(0))
  Option(zipFile.getEntry(args(1))) match {
    case Some(_) => ()
    case None => sys.error("File " + args(1) + " not found in zip " + args(0))
  }
}
