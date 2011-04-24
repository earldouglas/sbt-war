package sbt

import sbt._
import IO.{copyFile, wrapNull, createDirectory}

object oldcompat {
  def trapUnit(errorMessagePrefix: => String, log: Logger)(execute: => Option[String]): Option[String] =
    try { execute }
    catch { case e => log.trace(e); Some(errorMessagePrefix + e.toString) }

  def trap[T](errorMessagePrefix: => String, log: Logger)(execute: => Either[String, T]): Either[String, T] =
    try { execute }
    catch { case e => log.trace(e); Left(errorMessagePrefix + e.toString) }

  private def copyImpl(sources: Iterable[Path], destinationDirectory: Path, log: Logger)
  (doCopy: Path => Option[String]): Option[String] = {
    val target = destinationDirectory.asFile
    val creationError = 
      if(target.isDirectory)
	None
      else { 
	createDirectory(target)
        None
      }
    def copy(sources: List[Path]): Option[String] = {
      sources match
      {
	case src :: remaining => {
	  doCopy(src) match {
	    case None => copy(remaining)
	    case error => error
	  }
	}
	case Nil => None
      }
    }
    creationError orElse ( trapUnit("", log) { copy(sources.toList) } )
  }
  
  def copy(sources: Iterable[Path], destinationDirectory: Path, log: Logger): Either[String, Set[Path]] =
    copy(sources, destinationDirectory, false, log)

  /**
   * Equivalent to {@code copy(sources, destinationDirectory, overwrite, false, log)}.
   */
  def copy(sources: Iterable[Path], destinationDirectory: Path, overwrite: Boolean, log: Logger): Either[String, Set[Path]] =
    copy(sources, destinationDirectory, overwrite, false, log)

  /** Copies the files declared in <code>sources</code> to the <code>destinationDirectory</code>
   * directory.  Directories are not recursively entered.  The destination hierarchy matches the
   * source paths relative to any base directories.  For example:
   *
   * A source <code>(basePath ##) / x / y</code> is copied to <code>destinationDirectory / x / y</code>.
   *
   * @param overwrite if true, existing destination files are always overwritten
   * @param preserveLastModified if true, the last modified time of copied files will be set equal to
   * their corresponding source files.
   */
  def copy(sources: Iterable[Path], destinationDirectory: Path, overwrite: Boolean, preserveLastModified: Boolean, log: Logger): Either[String, Set[Path]] = {
    val targetSet = new scala.collection.mutable.HashSet[Path]
    copyImpl(sources, destinationDirectory, log)
    {
      source =>
	{
	  val from = source.asFile
	  val toPath = Path.fromString(destinationDirectory, source.relativePath)
	  targetSet += toPath
	  val to = toPath.asFile
	  if(!to.exists || overwrite || from.lastModified > to.lastModified)
	    {
	      val result: Option[String] =
		if(from.isDirectory) { 
		  createDirectory(to)
                  None
                }
		else
		  {
		    log.debug("Copying " + source + " to " + toPath)
		    copyFile(from, to)
                    None
		  }
	      if (result.isEmpty && preserveLastModified) { 
		to.setLastModified(from.lastModified)
                None
              }
	      else
		result
	    }
	      else
		None
	}
    }.toLeft(Set() ++ targetSet)
  }

  /** Copies the files declared in <code>sources</code> to the <code>targetDirectory</code>
   * directory.  The source directory hierarchy is flattened so that all copies are immediate
   * children of <code>targetDirectory</code>.  Directories are not recursively entered.*/
  def copyFilesFlat(sources: Iterable[File], targetDirectory: Path, log: Logger) = {
    require(targetDirectory.asFile.isDirectory, "Target '" + targetDirectory + "' is not a directory.")
    val byName = new scala.collection.mutable.HashMap[String, File]
    for(source <- sources) byName.put(source.getName, source)
    val uniquelyNamedSources = byName.values
    val targetSet = new scala.collection.mutable.HashSet[Path]
    def copy(source: File): Option[String] =
      {
	if(source.isDirectory)
	  copyAll(source.listFiles.toList)
	else if(source.exists)
	  {
	    val targetPath = targetDirectory / source.getName
	    targetSet += targetPath
	    if(!targetPath.exists || source.lastModified > targetPath.lastModified)
	      {
		log.debug("Copying " + source + " to " + targetPath)
		copyFile(source, targetPath.asFile)
                None
	      }
	    else
	      None
	  }
	    else
	      None
      }
    def copyAll(sources: List[File]): Option[String] =
      sources match
    {
      case head :: tail =>
	copy(head) match
      {
	case None => copyAll(tail)
	case x => x
      }
      case Nil => None
    }
    trap("Error copying files: ", log) { copyAll(uniquelyNamedSources.toList).toLeft(Set() ++ targetSet) }
  }
  /** Copies the files declared in <code>sources</code> to the <code>destinationDirectory</code>
   * directory.  The source directory hierarchy is flattened so that all copies are immediate
   * children of <code>destinationDirectory</code>.  Directories are not recursively entered.*/
  def copyFlat(sources: Iterable[Path], destinationDirectory: Path, log: Logger) = {
    val targetSet = new scala.collection.mutable.HashSet[Path]
    copyImpl(sources, destinationDirectory, log) {
      source => {
	val from = source.asFile
	val toPath = destinationDirectory / from.getName
	targetSet += toPath
	val to = toPath.asFile
	if(!to.exists || from.lastModified > to.lastModified && !from.isDirectory)
	  {
	    log.debug("Copying " + source + " to " + toPath)
	    copyFile(from, to)
            None
	  }
	else
	  None
      }
    }.toLeft(Set() ++ targetSet)
  }
  /** Deletes the given file recursively.*/
  def clean(file: Path, log: Logger): Option[String] = clean(file :: Nil, log)
  /** Deletes the given files recursively.*/
  def clean(files: Iterable[Path], log: Logger): Option[String] = clean(files, false, log)
  /** Deletes the given files recursively.  <code>quiet</code> determines the logging level.
   * If it is true, each file in <code>files</code> is logged at the <code>info</code> level.
   * If it is false, the <code>debug</code> level is used.*/
  def clean(files: Iterable[Path], quiet: Boolean, log: Logger): Option[String] =
    deleteFiles(Path.getFiles(files), quiet, log)
  
  private def deleteFiles(files: Iterable[File], quiet: Boolean, log: Logger): Option[String] =
    ((None: Option[String]) /: files)( (result, file) => result orElse delete(file, quiet, log))
  private def delete(file: File, quiet: Boolean, log: Logger): Option[String] =
    {
      def logMessage(message: => String)
      {
	log.log(if(quiet) Level.Debug else Level.Info, message)
      }
      trapUnit("Error deleting file " + file + ": ", log) {
	if(file.isDirectory) {
	  logMessage("Deleting directory " + file)
	  deleteFiles(wrapNull(file.listFiles), true, log)
	  file.delete
	}
	else if(file.exists) {
	  logMessage("Deleting file " + file)
	  file.delete
	}
        None
      }
    }
}
