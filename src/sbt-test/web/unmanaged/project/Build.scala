import sbt._
import WebPlugin._
import Keys._

object MyBuild extends Build {
	lazy val projects = Seq(root)

	lazy val root = Project("root", file("."), settings = Defaults.defaultSettings ++ webSettings ++ rootSettings)

	lazy val rootSettings = Seq(
		webappUnmanaged <<= temporaryWarPath( _ / "WEB-INF" / "appengine-generated" *** )
	)
}
