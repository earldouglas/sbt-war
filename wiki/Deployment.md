## Tomcat

On Ubuntu, install both *tomcat7* and *tomcat7-admin*:

```bash
sudo apt-get install tomcat7 tomcat7-admin
```

Create a Tomcat user with the role **manager-script** in */etc/tomcat7/tomcat-users.xml*:

```xml
<user username="manager" password="secret" roles="manager-script" />
```

Restart tomcat:

```bash
sudo service tomcat7 restart
```

Now a WAR file can be deployed using the Manager *deploy* command:

```
curl --upload-file myapp.war "http://manager:secret@myhost:8080/manager/text/deploy?path=/myapp&update=true"
```

The application will be available at *myhost:8080/myapp*.

Learn more about Manager commands [here](http://tomcat.apache.org/tomcat-7.0-doc/manager-howto.html).

## Heroku

1. Install the [Heroku Toolbelt](https://toolbelt.heroku.com/)

2. Install the `heroku-deploy` command line plugin:

```bash
heroku plugins:install https://github.com/heroku/heroku-deploy
```
3. Create a WAR file:

```bash
sbt package
```

4 Deploy the WAR file:

```bash
heroku deploy:war --war <path_to_war_file> --app <app_name>
```

See [devcenter.heroku.com](https://devcenter.heroku.com/articles/war-deployment) for more information.

## Google App Engine

See [developers.google.com](https://developers.google.com/appengine/docs/java/tools/uploadinganapp) for more information.

## Multiple contexts

Here is an example of how multiple webapps could be deployed to a single Jetty instance:

```scala
import sbt._

import com.github.siasia._
import WebappPlugin.webappSettings
import Keys._
    
object WebBuild extends Build {
  lazy val container = Container("container")

  lazy val rootSettings = Seq(
    libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
  ) ++ container.deploy(
    "/sub1" -> sub1,
    "/sub2" -> sub2
  )    
      
  lazy val sub1 = Project("sub1", file("sub1")) settings(webappSettings :_*)
  lazy val sub2 = Project("sub2", file("sub2")) settings(webappSettings :_*)
  lazy val root = Project("root", file(".")) settings(rootSettings :_*)
  override def projects = Seq(root, sub1, sub2)
}
```
    
Let's play with it a little

    > container:start
    2011-10-02 07:49:53.917:INFO::Logging to STDERR via org.mortbay.log.StdErrLog
    [info] jetty-6.1.22
    [info] NO JSP Support for /sub1, did not find org.apache.jasper.servlet.JspServlet
    [info] NO JSP Support for /sub2, did not find org.apache.jasper.servlet.JspServlet
    [info] Started SocketConnector@0.0.0.0:8080
    [success] Total time: 1 s, completed 02.10.2011 7:49:54
    > container:stop 
    [success] Total time: 0 s, completed 02.10.2011 7:54:34
    > container:start
    [info] jetty-6.1.22
    [info] NO JSP Support for /sub1, did not find org.apache.jasper.servlet.JspServlet
    [info] NO JSP Support for /sub2, did not find org.apache.jasper.servlet.JspServlet
    [info] Started SocketConnector@0.0.0.0:8080
    [success] Total time: 0 s, completed 02.10.2011 7:54:38
    > container:reload /sub 
    /sub1   /sub2
    > container:reload /sub1
    [info] NO JSP Support for /sub1, did not find org.apache.jasper.servlet.JspServlet
    [success] Total time: 0 s, completed 02.10.2011 7:54:53
    >
    
## Deploy different configurations

Simple examples:

Run whole webapp in Runtime conf:
```scala
webSettings(Runtime)
```

Instruct webapp to put it's deployment info into Runtime:
```scala
inConfig(Runtime)(webappSettings0)
```

Override conf which container takes deployment info from:
```scala
container.deploy(Runtime)
```

More complex:

```scala
import sbt._
import Keys._
import com.github.siasia._
import PluginKeys._
import WebPlugin._
import WebappPlugin._

object WebBuild extends Build {
  def rootSettings = webappSettings ++
  inConfig(Runtime)(webappSettings0) ++
  container.settings ++ Seq(    
    apps <<= (deployment in Compile, deployment in Runtime) map {
      (compile, runtime) => Seq(
        "/compile" -> compile,
        "/runtime" -> runtime
      )
    },
    libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
  )
  
  lazy val root = Project("root", file(".")) settings(rootSettings :_*)
}
```

## Additional containers

```scala
import sbt._
import Keys._
import com.github.siasia._
import PluginKeys._
import WebPlugin._
import WebappPlugin._

object WebBuild extends Build {
  val additional = Container("additional")
  
  def rootSettings: Seq[Setting[_]] =
    container.deploy(
      "/" -> subRef
    ) ++
    additional.deploy(
      "/" -> subRef
    ) ++
    Seq(
      libraryDependencies ++= Seq(
        "org.mortbay.jetty" % "jetty" % "6.1.22" % "container",
        "org.mortbay.jetty" % "jetty" % "6.1.22" % "additional"
      ),
      port in additional.Configuration := 8081
    )

  lazy val sub = Project("sub", file("sub")) settings(webappSettings :_*)
  lazy val subRef: ProjectReference = sub
  lazy val root = Project("root", file(".")) settings(rootSettings :_*)
  override def projects = Seq(root, sub)
}
```
