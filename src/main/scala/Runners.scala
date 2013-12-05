package com.earldouglas.xsbtwebplugin

import sbt._
import classpath._
import scala.reflect.Manifest
import java.net.URL

object Runners {
  /**
   * This classloader searches itself for a given Runner class (including inner
   * classes) and delegates to the parent loader for all other classes.  This
   * allows us load the Runners using a classloader that includes the container
   * dependencies.  Otherwise the Runner classes would be loaded from the
   * classloader used to run this plugin.
   */
  private class RunnerLoader(runnerClassName: String, urls: Array[URL],
                            parent: ClassLoader) extends LoaderBase(urls, parent) {
    def doLoadClass(className: String): Class[_] = {
      if (isRunnerClass(className))
        findClass(className)
      else
        parent.loadClass(className)
    }
    
    private def isRunnerClass(className: String) =
      className == runnerClassName || className.startsWith(runnerClassName + "$")
  }
  
  private def filters(packages: Seq[String]): ((String) => Boolean, (String) => Boolean) = {
    def filter(name: String) = packages.exists(p => name.startsWith(p + "."))
    def notFilter(name: String) = !filter(name)

    (filter, notFilter)
  }

  def makeInstance[If: Manifest](loader: ClassLoader, packages: Seq[String], implName: String): If = {
    val baseLoader = manifest[If].runtimeClass.getClassLoader
    val (filter, notFilter) = filters(packages)
    
    // A loader that can load both container classes and SBT classes
    val dualLoader = new DualLoader(baseLoader, notFilter, x => true, loader, filter, x => false)
    // A loader than can load the Runner classes and their dependencies
    val runnerLoader = new RunnerLoader(implName, Array(IO.classLocation[If].toURI.toURL), dualLoader)

    val cls = Class.forName(implName, true, runnerLoader)
    cls.getConstructor().newInstance().asInstanceOf[If]
  }

  def makeInstance[If,Im <: If](loader: ClassLoader, packages: Seq[String])(implicit ImM: Manifest[Im]): If =
    makeInstance(loader, packages, ImM.toString)
}
