# xsbt-web-plugin

xsbt-web-plugin is an [sbt] extension for building [J2EE][j2ee] Web
applications in Scala and Java.  It is best suited for projects that:

* Deploy to common cloud platforms (e.g. [Google App Engine][gae],
  [Heroku][heroku], [Elastic Beanstalk][ebs], [Jelastic][jelastic])
* Deploy to production J2EE environments (e.g. Tomcat, Jetty, GlassFish,
  WebSphere)
* Incorporate J2EE libraries (e.g. [JSP][jsp], [JSF][jsf], [EJB][ejb])
* Utilize J2EE technologies (e.g. [`Servlet`][servlet],
  [`Filter`][filter], [JNDI][jndi])
* Have a specific need to be packaged as a [*.war* file][war]

## Reference manual

Documentation is available by minor release version.  Version numbers
follow the [Specified Versioning][specver] guidelines.

### Current release

* [2.3.x](https://github.com/earldouglas/xsbt-web-plugin/blob/master/docs/2.3.md)

### Previous releases

* [2.2.x](https://github.com/earldouglas/xsbt-web-plugin/blob/master/docs/2.2.md)
* [2.1.x](https://github.com/earldouglas/xsbt-web-plugin/blob/master/docs/2.1.md)
* [2.0.x](https://github.com/earldouglas/xsbt-web-plugin/blob/master/docs/2.0.md)
* [1.1.x](https://github.com/earldouglas/xsbt-web-plugin/blob/master/docs/1.1.md)
* [1.0.x](https://github.com/earldouglas/xsbt-web-plugin/blob/master/docs/1.0.md)
* [0.9.x](https://github.com/earldouglas/xsbt-web-plugin/blob/master/docs/0.9.md)

[ebs]: https://console.aws.amazon.com/elasticbeanstalk/home
[ejb]: http://en.wikipedia.org/wiki/Ejb
[filter]: http://docs.oracle.com/javaee/6/api/javax/servlet/Filter.html
[gae]: https://developers.google.com/appengine/
[heroku]: https://www.heroku.com/
[j2ee]: http://en.wikipedia.org/wiki/Java_Servlet
[jelastic]: http://jelastic.com/
[jndi]: http://en.wikipedia.org/wiki/Java_Naming_and_Directory_Interface
[jsf]: http://en.wikipedia.org/wiki/JavaServer_Faces
[jsp]: http://en.wikipedia.org/wiki/JavaServer_Pages
[sbt]: http://www.scala-sbt.org/
[servlet]: http://docs.oracle.com/javaee/6/api/javax/servlet/Servlet.html
[specver]: https://earldouglas.com/posts/specver.html
[war]: https://en.wikipedia.org/wiki/WAR_%28Sun_file_format%29
