package sbt

import sbt._
import IO.{copy, delete, wrapNull, createDirectory}

object oldcompat {
  private def trap[T](log: Logger)(execute: => T): Either[String, T] =
    try { Right(execute) }
    catch { case e => log.trace(e); Left(e.toString) }

  private def trapUnit(log: Logger)(execute: => Unit): Option[String] =
    try { execute; None }
    catch { case e => log.trace(e); Some(e.toString) }

  def copy(sources: Iterable[Path], destinationDirectory: Path, log: Logger): Either[String, Set[Path]] = {
    val map = sources.map(source => (source.asFile, Path.fromString(destinationDirectory, source.relativePath).asFile))
    trap(log)(IO.copy(map))
  }

  def copyFlat(sources: Iterable[Path], destinationDirectory: Path, log: Logger): Either[String, Set[Path]] = {
    val map = sources.map(source => (source.asFile, Path.fromString(destinationDirectory, source.getName).asFile))
    trap(log)(IO.copy(map))
  }
  
  def clean(files: Iterable[Path], log: Logger): Option[String] =
    trapUnit(log)(delete(Path.getFiles(files)))
}
