resolvers += Resolver.url("Typesafe repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)

libraryDependencies <<= (libraryDependencies, appConfiguration) { (deps, app) => deps :+ "org.scala-tools.sbt" %% "scripted-plugin" % app.provider.id.version }
