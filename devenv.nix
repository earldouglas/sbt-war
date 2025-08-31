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

  languages.java.jdk.package = pkgs.jdk11;
  languages.scala.enable = true;
  languages.scala.sbt.enable = true;

  scripts.sbt-fmt.exec = ''
    sbt \
      scalafixAll \
      javafmtAll \
      scalafmtAll \
      scalafmtSbt
  '';

  scripts.sbt-test.exec = ''
    sbt \
      javafmtCheckAll \
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
