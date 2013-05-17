package com.earldouglas.xsbtwebplugin

import sbt._
import classpath._
import scala.reflect.Manifest
import java.net.URL
  
object LazyLoader {
  object Loaders
  {
    val SbtPackage = "sbt."
    def isNestedOrSelf(className: String, checkAgainst: String) =
      className == checkAgainst || className.startsWith(checkAgainst + "$")
    def isSbtClass(className: String) = className.startsWith(Loaders.SbtPackage)
  }

  class LazyFrameworkLoader(runnerClassName: String, urls: Array[URL], parent: ClassLoader, grandparent: ClassLoader)
  extends LoaderBase(urls, parent)
  {
    def doLoadClass(className: String): Class[_] =
    {
      if(Loaders.isNestedOrSelf(className, runnerClassName))
        findClass(className)
      else if(Loaders.isSbtClass(className)) // we circumvent the parent loader because we know that we want the
        grandparent.loadClass(className)     // version of sbt that is currently the builder (not the project being built)
      else
        parent.loadClass(className)
    }
  }
  
  private def filters(packages: Seq[String]): ((String) => Boolean, (String) => Boolean) = {
    def filter(name: String) =
      packages.foldLeft(false) {
        (acc, p) => acc || name.startsWith(p + ".")
      }
    def notFilter(name: String) =
        !filter(name)
    (filter, notFilter)
  }

  def makeInstance[If: Manifest](loader: ClassLoader, packages: Seq[String], implName: String): If = {
    val base = manifest[If].erasure.getClassLoader
    val (filter, notFilter) = filters(packages)

    val dual = new DualLoader(base, notFilter, x => true, loader, filter, x => false)    
    val lazyLoader = new LazyFrameworkLoader(implName, Array(IO.classLocation[If].toURI.toURL), dual, base)
    val cls = Class.forName(implName, true, lazyLoader)
    cls.getConstructor().newInstance().asInstanceOf[If]
  }
    
  def makeInstance[If,Im <: If](loader: ClassLoader, packages: Seq[String])(implicit ImM: Manifest[Im]): If = makeInstance(loader, packages, ImM.toString)    
}
