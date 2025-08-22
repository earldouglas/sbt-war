val warFile = taskKey[File]("warFile")

warFile :=
  fileConverter.value
    .toPath(sbt.Keys.`package`.value)
    .toFile()
