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
    *   a mapping from destination to source of webapp resources
    */
  def getResources(resourcesDir: File): Map[String, File] = {
    (resourcesDir ** "*").get
      .filter(_.exists())
      .filter(_.isFile())
      .flatMap(file =>
        IO
          .relativize(resourcesDir, file)
          .map(path => path -> file)
      )
      .toMap
  }

  /** Given a classpath (potentially with both .jar files and classes
    * directories), traverse to find all the .class files.
    *
    * @return
    *   a mapping from destination to source of .class files
    */
  def getClasses(classpath: Seq[File]): Map[String, File] = {

    val classpathDirs: Seq[File] =
      classpath
        .filter(_.exists())
        .filter(_.isDirectory())

    val classesMappings: Seq[(String, File)] =
      for {
        classpathDir <- classpathDirs
        classFile <- (classpathDir ** "*").get
        if classFile.exists()
        if classFile.isFile()
        relativeFile <- IO.relativizeFile(classpathDir, classFile)
        relativePath = s"WEB-INF/classes/${relativeFile.getPath()}"
      } yield (relativePath, classFile)

    classesMappings.toMap
  }

  /** Given a classpath (potentially with both .jar files and classes
    * directories), traverse to find all the .jar files.
    *
    * @return
    *   a mapping from destination to source of .jar files
    */
  def getLib(classpath: Seq[File]): Map[String, File] = {
    classpath
      .filter(f => f.exists())
      .filter(f => f.isFile())
      .filter(f => f.getName().endsWith(".jar"))
      .map(file => s"WEB-INF/lib/${file.getName()}" -> file)
      .toMap
  }
}
