package com.earldouglas;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class WarConfiguration {

  /** The port to use for the server, e.g. 8080 */
  public final int port;

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
   *   <li>port
   *   <li>warFile
   * </ul>
   *
   * <p>Example:
   *
   * <pre>
   * port=8989
   * warFile=path/to/warfile.war
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
        Integer.parseInt(properties.getProperty("port")),
        new File(properties.getProperty("warFile")));
  }

  /**
   * Construct a new configuration from the given parameters.
   *
   * @param port the port to use for the server, e.g. 8080
   * @param warFile the WAR file to serve
   */
  public WarConfiguration(final int port, final File warFile) {
    this.port = port;
    this.warFile = warFile;
  }
}
