{
  pkgs,
  lib,
  config,
  inputs,
  ...
}:
{

  packages = [
    pkgs.git
  ];

  languages.java.jdk.package = pkgs.jdk17;
  languages.scala.enable = true;
  languages.scala.sbt.enable = true;

  scripts.sbt-fmt.exec = ''
    sbt \
      scalafixAll \
      scalafmtAll \
      scalafmtSbt
  '';

  scripts.sbt-test.exec = ''
    sbt \
      scalafmtCheckAll \
      "scalafixAll --check" \
      +test \
      publishLocal \
      +scripted
  '';

  enterTest = ''
    sbt-test
  '';

}
