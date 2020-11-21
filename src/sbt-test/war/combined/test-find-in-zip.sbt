val findInZip = InputKey[Unit]("find-in-zip")

findInZip := {
  val args: Seq[String] = Def.spaceDelimited("<arg>").parsed
  val zipFile = new java.util.zip.ZipFile(args(0))
  Option(zipFile.getEntry(args(1))) match {
    case Some(_) =>
      ()
    case None =>
      val entries = zipFile.entries()
      var contents: Set[String] = Set.empty
      while (entries.hasMoreElements()) {
        val entry = entries.nextElement()
        contents = contents + entry.getName()
      }
      var contentsS = contents.toList.sorted.map(n => s"  ${n}").mkString("\n")
      sys.error(s"File ${args(1)} not found in zip file ${args(0)}:\n\n${contentsS}")
  }
}
