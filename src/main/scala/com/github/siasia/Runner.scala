package com.github.siasia

import sbt._
import classpath._
import ClasspathUtilities._
import java.net.{URI, URL, URLClassLoader}
import java.lang.reflect.InvocationTargetException

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
			grandparent.loadClass(className)              // version of sbt that is currently the builder (not the project being built)
		else
			parent.loadClass(className)
	}
}

object Runner {
	def runners = Seq(
		"Jetty6Runner",
		"Jetty7Runner"
	)
	def apply(instance: ScalaInstance, classpath: Seq[File]): Runner = {
		val base = getClass.getClassLoader
		val parentLoader = instance.loader
		val loader: ClassLoader = toLoader(classpath, parentLoader)

		val filter = (name: String) => name.startsWith("org.mortbay.") || name.startsWith("org.eclipse.jetty.")
		val notFilter = (name: String) => !filter(name)

		val dual = new DualLoader(base, notFilter, x => true, loader, filter, x => false)		
		val runner = guessRunner(base, dual, runners)
		runner.setLoader(loader)
		runner
	}
	def loadRunner(className: String, base: ClassLoader, dual: ClassLoader):Runner = {
		val runner = "com.github.siasia."+className
		val lazyLoader = new LazyFrameworkLoader(runner, Array(IO.classLocation[Runner].toURI.toURL), dual, base)
		val cls = Class.forName(runner, true, lazyLoader).asSubclass(classOf[Runner])
		cls.getConstructor().newInstance()
	}
	def guessRunner(base: ClassLoader, dual: ClassLoader, rs: Seq[String]): Runner = rs match {
		case Seq() => sys.error("Unable to load runner")
		case Seq(runner, rest@_*) =>
			try { loadRunner(runner, base, dual)	}
			catch {
				case e: InvocationTargetException =>
					e.getCause match {
						case _: NoClassDefFoundError =>
							guessRunner(base, dual, rest)
					}
				case e: NoClassDefFoundError =>
					guessRunner(base, dual, rest)
			}			
	}
}

trait Runner {
	protected var loader: ClassLoader = null
	def setLoader(loader: ClassLoader) { this.loader = loader }
	def start(port: Int, logger: AbstractLogger, apps: Seq[(String, Deployment)]): Unit
	def reload(context: String): Unit
	def stop(): Unit
}

class LoggerBase(delegate: AbstractLogger)
{
	def getName = "ContainerLogger"
	def isDebugEnabled = delegate.atLevel(Level.Debug)
	def setDebugEnabled(enabled: Boolean) = delegate.setLevel(if(enabled) Level.Debug else Level.Info)

	def ignore(th: Throwable) { delegate.trace(th) }
	def info(th: Throwable) { delegate.trace(th) }
	def debug(th: Throwable) { delegate.trace(th) }
	def warn(th: Throwable) { delegate.trace(th) }
	def info(msg: String) { delegate.info(msg) }
	def debug(msg: String) { delegate.warn(msg) }
	def warn(msg: String) { delegate.warn(msg) }
	def info(msg: String, arg0: AnyRef, arg1: AnyRef) { delegate.info(format(msg, arg0, arg1)) }
	def debug(msg: String, arg0: AnyRef, arg1: AnyRef) { delegate.debug(format(msg, arg0, arg1)) }
	def warn(msg: String, arg0: AnyRef, arg1: AnyRef) { delegate.warn(format(msg, arg0, arg1)) }
	def info(msg: String, args: AnyRef*) { delegate.info(format(msg, args: _*)) }
	def debug(msg: String, args: AnyRef*) { delegate.debug(format(msg, args: _*)) }
	def warn(msg: String, args: AnyRef*) { delegate.warn(format(msg, args: _*)) }
	def info(msg: String, th: Throwable)
	{
		delegate.info(msg)
		delegate.trace(th)
	}
	def debug(msg: String, th: Throwable)
	{
		delegate.debug(msg)
		delegate.trace(th)
	}
	def warn(msg: String, th: Throwable)
	{
		delegate.warn(msg)
		delegate.trace(th)
	}
	private def format(msg: String, args: AnyRef*) =
	{
		def toString(arg: AnyRef) = if(arg == null) "" else arg.toString
		val pieces = msg.split("""\{\}""", args.length + 1).toList
		val argStrs = args.map(toString).toList ::: List("")
		pieces.zip(argStrs).foldLeft(new StringBuilder) { (sb, pair) =>
			val (piece, argStr) = pair
			if (piece.isEmpty) sb
			else sb.append(piece).append(argStr)
		}.toString
	}
}
