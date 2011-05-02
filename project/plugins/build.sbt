resolvers ++= Seq(
	"Scripted Repo" at "http://siasia.github.com/maven2"
)

libraryDependencies <<= (libraryDependencies, appConfiguration) { (deps, app) => deps :+ "org.scala-tools.sbt" %% "scripted-plugin" % app.provider.id.version }
