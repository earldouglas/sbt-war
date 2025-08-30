val warFile = taskKey[File]("warFile")

warFile := sbt.Keys.`package`.value
