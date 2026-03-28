val warFile = taskKey[File]("warFile")

warFile :=
  Def.uncached:
    fileConverter.value
      .toPath(sbt.Keys.`package`.value)
      .toFile()
