package com.github.siasia

${imports}

import sbt._
import classpath.ClasspathUtilities.toLoader

class Jetty${version}Runner extends Runner {
	private[this] val forceJettyLoad = classOf[Server]
	private var server: Server = null
	private var contexts: Map[String, (WebAppContext, Deployment)] = Map()
	private def setContextLoader(context: WebAppContext, classpath: Seq[File]) {
		val appLoader = toLoader(classpath, loader)
		context.setClassLoader(appLoader)
	}	
	private def deploy(contextPath: String, deployment: Deployment) = {
		import deployment._
		val context = new WebAppContext()
		context.setContextPath(contextPath)
		context.setBaseResource(
			new ResourceCollection(
				webappResources.map(_.getPath).toArray
			))
		setContextLoader(context, classpath)
		contexts += contextPath -> (context, deployment)
		context
	}	
	def start(port: Int, logger: AbstractLogger, apps: Seq[(String, Deployment)]) {
		if(server != null)
			return
		Log.setLog(delegatingLogger(logger))
		server = new Server(port)
		val contexts = apps.map { case (contextPath, deployment) => deploy(contextPath, deployment) }
		val coll = new ContextHandlerCollection()
		coll.setHandlers(contexts.toArray)
		server.setHandler(coll)
		server.start()
	}
	def reload(contextPath: String) {
		val (context, deployment) = contexts(contextPath)
		context.stop()
		setContextLoader(context, deployment.classpath)
		context.start()
	}
	def stop() {
		if(server != null)
			server.stop()
		server = null
	}
	def delegatingLogger(delegate: AbstractLogger) = new LoggerBase(delegate) with JLogger {
		def getLogger(name: String) = this
	}
}


