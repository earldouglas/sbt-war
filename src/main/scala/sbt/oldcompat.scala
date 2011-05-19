package sbt

import sbt._
import IO.{copy, delete, wrapNull, createDirectory}

object oldcompat {
  def copyFlat(sources: Iterable[File], destinationDirectory: File): Set[File] = {
    val map = sources.map(source => (source.asFile, destinationDirectory / source.getName))
    IO.copy(map)
  }
}
