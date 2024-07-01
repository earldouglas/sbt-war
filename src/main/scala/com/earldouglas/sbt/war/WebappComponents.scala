package com.earldouglas.sbt.war

import sbt._

/** Identifies the files that compose the webapp:
  *
  *   - Resources
  *     - HTML/JS/CSS files, images, etc.
  *     - Optional WEB-INF/web.xml deployment descriptor
  *     - Anything else that isn't a .class file or .jar file
  *   - .class files
  *   - .jar files
  */
object WebappComponents {

  /** Given a resources directory, e.g. src/main/webapp, traverse to
    * find all the files it contains.
    *
    * @return
    *   a mapping from source to destination of webapp resources
    */
  def getResources(resourcesDir: File): Map[File, String] = {
    (resourcesDir ** "*").get
      .filter(_.isFile())
      .flatMap(src =>
        IO
          .relativize(resourcesDir, src)
          .map(dst => src -> dst)
      )
      .toMap
  }

  /** Given a classpath (potentially with both .jar files and classes
    * directories), traverse to find all the .class files.
    *
    * @return
    *   a mapping from source to destination of .class files
    */
  def getClasses(classpath: Seq[File]): Map[File, String] = {

    val classpathDirs: Seq[File] =
      classpath
        .filter(_.isDirectory())

    val classesMappings: Seq[(File, File)] =
      for {
        classpathDir <- classpathDirs
        classFile <- (classpathDir ** "*").get
        if classFile.isFile()
        relativeFile <- IO.relativizeFile(classpathDir, classFile)
      } yield (classFile, relativeFile)

    classesMappings
      .map({ case (src, dst) => src -> dst.getPath() })
      .toMap
  }

  /** Given a classpath (potentially with both .jar files and classes
    * directories), traverse to find all the .jar files.
    *
    * @return
    *   a mapping from source to destination of .jar files
    */
  def getLib(classpath: Seq[File]): Map[File, String] = {
    classpath
      .filter(f => f.isFile())
      .filter(f => f.getName().endsWith(".jar"))
      .map(src => src -> src.getName())
      .toMap
  }
}
