{ pkgs ? import <nixpkgs> {} }:
  pkgs.mkShell {
    nativeBuildInputs = [
      (pkgs.sbt.override { jre = pkgs.jdk11; })
    ];
}
