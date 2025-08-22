{
  pkgs,
  lib,
  config,
  inputs,
  ...
}:
let

  jdk = pkgs.jdk11;

  sbt = pkgs.sbt.override {
    jre = jdk;
  };

in
{

  packages = [
    sbt
    pkgs.git
  ];

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
