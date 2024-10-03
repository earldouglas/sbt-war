{ pkgs ? import <nixpkgs> {} }:
let
  jdk = pkgs.jdk11;
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
