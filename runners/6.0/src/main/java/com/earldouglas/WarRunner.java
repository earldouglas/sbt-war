package com.earldouglas;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jetty.ee10.annotations.AnnotationConfiguration;
import org.eclipse.jetty.ee10.webapp.MetaInfConfiguration;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.Server;

public class WarRunner {

  /**
   * Load configuration from the file in the first argument, and use it to start a new WarRunner.
   *
   * @param args the configuration filename to load and run
   * @throws Exception if something goes wrong
   */
  public static void main(final String[] args) throws Exception {

    final WarConfiguration warConfiguration = WarConfiguration.load(args[0]);

    final Path warPath = Paths.get(warConfiguration.warFile.getPath()).toAbsolutePath().normalize();

    final Server server = new Server(warConfiguration.port);

    final WebAppContext webapp = new WebAppContext();

    webapp.getConfigurations().add(new AnnotationConfiguration());

    webapp.setParentLoaderPriority(false);

    webapp.setAttribute(
      MetaInfConfiguration.CONTAINER_JAR_PATTERN,
      ".*/lib/[^/]*\\.jar$|.*/classes/.*|.*/test-classes/.*"
    );

    webapp.setAttribute(
      MetaInfConfiguration.WEBINF_JAR_PATTERN ,
      ".*\\.jar$|.*/lib/[^/]*\\.jar$|.*/classes/.*|.*/test-classes/.*"
    );
    // context.getMetaData().addContainerResource(resource);

    webapp.setContextPath("/");

    System.err.println();
    System.err.println();
    System.err.println();
    System.err.println(warPath.toFile().getPath());
    System.err.println();
    System.err.println();
    System.err.println();
    Thread.sleep(2000);

    //webapp.setWar(warPath.toUri().toASCIIString());
    webapp.setWar("/home/james/tmp/sbt_ee4a361f_3-0.1.0-SNAPSHOT.war");

    server.setHandler(webapp);

    server.start();
    server.join();
  }
}
