package com.earldouglas;

import java.nio.file.Path;
import java.nio.file.Paths;
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
    webapp.setContextPath("/");
    webapp.setWar(warPath.toUri().toASCIIString());

    server.setHandler(webapp);

    server.start();
    server.join();
  }
}
