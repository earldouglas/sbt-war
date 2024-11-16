{ pkgs ? import <nixpkgs> {} }:
let
  jdk = pkgs.jdk17;
in
  pkgs.mkShell {
    nativeBuildInputs = [
      (pkgs.sbt.override { jre = jdk; })
    ];
    shellHook = ''
      export JAVA_HOME=${jdk}
      PATH="${jdk}/bin:$PATH"
    '';
  }
