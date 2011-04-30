resolvers += Resolver.url("Sbt repository", new java.net.URL("http://siasia.github.com/ivy2/"))(Resolver.ivyStylePatterns)

libraryDependencies <<= (libraryDependencies, appConfiguration) {
	(deps, app) =>
	deps :+ "com.github.siasia" %% "xsbt-web-plugin" % app.provider.id.version
}
