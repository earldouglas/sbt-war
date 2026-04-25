package com.earldouglas;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class WarConfiguration {

  /** The hostname to use for the server, e.g. "localhost" */
  public final String hostname;

  /** The port to use for the server, e.g. 8080 */
  public final int port;

  /**
   * The context path to use for the webapp.
   *
   * <p>For the root context path, use the empty string "".
   */
  public final String contextPath;

  /** The WAR file to serve. */
  public final File warFile;

  /**
   * Read configuration from a file at the specified location.
   *
   * @param configurationFilename the configuration filename to load
   * @throws IOException if something goes wrong
   * @return WarConfiguration a loaded configuration
   */
  public static WarConfiguration load(final String configurationFilename) throws IOException {
    return WarConfiguration.load(new File(configurationFilename));
  }

  /**
   * Read configuration from a file at the specified location.
   *
   * <p>The format of the file is a Properties file with the following fields:
   *
   * <ul>
   *   <li>hostname
   *   <li>port
   *   <li>warFile
   *   <li>contextPath
   * </ul>
   *
   * <p>Example:
   *
   * <pre>
   * hostname=localhost
   * port=8984
   * warFile=path/to/warfile.war
   * contextPath=
   * </pre>
   *
   * @param configurationFile the configuration file to load
   * @throws IOException if something goes wrong
   * @return WarConfiguration a loaded configuration
   */
  public static WarConfiguration load(final File configurationFile) throws IOException {

    final InputStream inputStream = new FileInputStream(configurationFile);

    final Properties properties = new Properties();
    properties.load(inputStream);

    return new WarConfiguration(
        properties.getProperty("hostname"),
        Integer.parseInt(properties.getProperty("port")),
        properties.getProperty("contextPath"),
        new File(properties.getProperty("warFile")));
  }

  /**
   * Construct a new configuration from the given parameters.
   *
   * @param hostname the hostname to use for the server, e.g. "localhost"
   * @param port the port to use for the server, e.g. 8080
   * @param contextPath the context path to use for the webapp, e.g. ""
   * @param warFile the WAR file to serve
   */
  public WarConfiguration(
      final String hostname,
      final int port,
      final String contextPath,
      final File warFile) {
    this.hostname = hostname;
    this.port = port;
    this.warFile = warFile;
    this.contextPath = contextPath;
  }
}
