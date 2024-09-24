package com.earldouglas.sbt.war

import sbt.File

/** Identifies the files that get bundled in the .war file:
  *
  *   - Resources
  *     - HTML/JS/CSS files, images, etc.
  *     - Optional WEB-INF/web.xml deployment descriptor
  *     - Anything else that isn't a .class file or .jar file
  *   - .class files in the classes/ directory
  *   - .jar files in the lib/ directory
  */
object WarPackage {

  /** Given the resources, classes, and .jar files of a webapp, put them
    * all together in the right subdirectories for the .war file.
    *
    * @return
    *   a mapping from source to destination of war contents
    */
  def getWarContents(
      webappResources: Map[File, String],
      webappClasses: Map[File, String],
      webappLib: Map[File, String]
  ): Seq[(File, String)] =
    Seq(
      webappResources,
      webappClasses.map { case (k, v) => k -> s"classes/${v}" },
      webappLib.map { case (k, v) => k -> s"lib/${v}" }
    ).flatten
}
