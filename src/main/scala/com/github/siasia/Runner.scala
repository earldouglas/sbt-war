package com.github.siasia

import sbt._
import classpath._
import ClasspathUtilities._
import java.net.{URI, URL, URLClassLoader}
import java.lang.reflect.InvocationTargetException
import scala.xml.NodeSeq

object Runner {
	def runners = Seq(
		classOf[Jetty6Runner].getName,
		classOf[Jetty7Runner].getName
	)
	def packages = Seq("org.mortbay", "org.eclipse.jetty")
	def apply(instance: ScalaInstance, classpath: Seq[File]): Runner = {
		val parentLoader = instance.loader
		val loader: ClassLoader = toLoader(classpath, parentLoader)

		val runner = guessRunner(loader, runners)
		runner.setLoader(loader)
		runner
	}
	def loadRunner(className: String, loader: ClassLoader):Runner =
		LazyLoader.makeInstance[Runner](loader, packages, className)
		
	def guessRunner(loader: ClassLoader, rs: Seq[String]): Runner = rs match {
		case Seq() => sys.error("Jetty dependencies should be on container classpath")
		case Seq(runner, rest@_*) =>
			try { loadRunner(runner, loader)	}
			catch {
				case e: InvocationTargetException =>
					e.getCause match {
						case _: NoClassDefFoundError =>
							guessRunner(loader, rest)
					}
				case e: NoClassDefFoundError =>
					guessRunner(loader, rest)
			}			
	}
}

trait Runner {
	protected var loader: ClassLoader = null
	def setLoader(loader: ClassLoader) { this.loader = loader }
	def start(port: Int, logger: AbstractLogger, apps: Seq[(String, Deployment)], customConf: Boolean, confFiles: Seq[File], confXml: NodeSeq): Unit
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
