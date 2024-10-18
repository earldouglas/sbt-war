package com.earldouglas;

public class WarRunner {

  /**
   * Load configuration from the file in the first argument, and use it to start a new WarRunner.
   *
   * @param args the configuration filename to load and run
   * @throws Exception if something goes wrong
   */
  public static void main(final String[] args) throws Exception {

    final WarConfiguration warConfiguration = WarConfiguration.load(args[0]);

    final String[] warRunnerArgs =
        new String[] {
          "--port", Integer.toString(warConfiguration.port), warConfiguration.warFile.getPath(),
        };

    webapp.runner.launch.Main.main(warRunnerArgs);
  }
}
