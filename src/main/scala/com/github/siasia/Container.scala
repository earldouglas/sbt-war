package com.github.siasia

import sbt._
import Keys._
import PluginKeys._
import Classpaths.managedJars
import Project.Initialize
import Scoped._
import sbinary.DefaultProtocol.StringFormat
import Cache.seqFormat
import complete._
import scala.xml.NodeSeq

case class Container(name: String) {
	def Configuration = config(name) hide
	def attribute = AttributeKey[Runner](name)
	def runner = attribute

	private implicit def attributeToRunner[Runner](key: AttributeKey[Runner])(implicit state: State):Runner = state.get(key).get

	private implicit def stateToRunner(state: State): Runner = state.get(attribute).get

	object Impl { 
		def eval[T](key: Project.ScopedKey[sbt.Task[T]])(implicit state: State):T =
			EvaluateTask.processResult(
				Project.evaluateTask(key, state) getOrElse
					sys.error("Error getting " + key),
				CommandSupport.logger(state))

		private implicit def keyToResult[T](key: ScopedTask[T])(implicit state: State): T = eval(key)

		def newRunner(ref: ProjectRef, state: State) = {
			implicit val s = state
			val classpath = Build.data(fullClasspath in (ref, Configuration))
			state.put(attribute, Runner(scalaInstance in ref, classpath))
		}
	}

	def globalSettings = Seq(
		ivyConfigurations += Configuration
	)
	
	def containerSettings = Seq(
		managedClasspath <<= (classpathTypes, update) map {(ct, up) => managedJars(Configuration, ct, up)},
		fullClasspath <<= managedClasspath,
		onLoad in Global <<= (onLoad in Global, thisProjectRef) {
			(onLoad, containerProject) =>
				(state) =>
					Impl.newRunner(containerProject, onLoad(state))},
		onUnload in Global <<= (onUnload in Global) {
			(onUnload) =>
				(state) =>
					state.stop()
					onUnload(state)
		},
		port := 8080,
		sslPort := 0,
		sslKeystore := None,
		sslPassword := None,
		start <<= (state, port, sslPort, sslKeystore, sslPassword, apps, customConfiguration, configurationFiles, configurationXml) map {
			(state, port, sslPort, sslKeystore, sslPassword, apps, cc, cf, cx) => {
			  val ssl = new SslSettings(sslPort, 
						    sslKeystore, 
						    sslPassword,
						    sslPassword
			  )
			  state.start(port, ssl, CommandSupport.logger(state).asInstanceOf[AbstractLogger], apps, cc, cf, cx)
			}
		},
		discoveredContexts <<= TaskData.write(apps map discoverContexts) triggeredBy start,
		reload <<= reloadTask(state),
		stop <<= (state) map {
			(state) =>
			state.stop()
		},
		customConfiguration := false,
		configurationFiles := Seq(),
		configurationXml := NodeSeq.Empty
	)
	def settings = globalSettings ++ inConfig(Configuration)(containerSettings)

	def pairToTask(conf: Configuration)(p: (String, ProjectReference)): Initialize[Task[(String, Deployment)]] = (deployment in (p._2, conf)) map { (d) => (p._1, d) }
	type SettingSeq = Seq[Setting[_]]
	def deploy(map: (String, ProjectReference)*): SettingSeq =
		deploy(DefaultConf)(map :_*)
	def deploy(conf: Configuration)(map: (String, ProjectReference)*): SettingSeq =
		settings ++ inConfig(Configuration)(Seq(
			apps <<= map.map(pairToTask(conf)).join
		))
	def discoverContexts(apps: Seq[(String, Deployment)]) = apps.map(_._1)
	def reloadParser: (State, Seq[String]) => Parser[String] =
	{
		import DefaultParsers._
		(state, contexts) => Space ~> token(NotSpace examples contexts.toSet)
	}
	def reloadTask(state: TaskKey[State]): Initialize[InputTask[Unit]] = {
		import DefaultParsers._
		InputTask(TaskData(discoveredContexts)(reloadParser)(Nil)) {
			result =>
				(state, result) map {
					(state, context) =>
					state.reload(context)
			}
		}
	}
}
