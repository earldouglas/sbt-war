resolvers ++= Seq(
	Resolver.url("Sbt repository", new java.net.URL("http://siasia.github.com/ivy2/"))(Resolver.ivyStylePatterns),
	"Scripted Repo" at "http://siasia.github.com/maven2"
)

libraryDependencies <<= (libraryDependencies, appConfiguration) { (deps, app) => deps :+ "org.scala-tools.sbt" %% "scripted-plugin" % app.provider.id.version }
