{
  pkgs ? import <nixpkgs> { },
}:

let

  jdk = pkgs.openjdk17;

  derivations = builtins.fetchGit {
    url = "https://git.earldouglas.com/earldouglas/derivations.git";
    rev = "f51ff3ba66bd0fa32c9ec6246a39a0d2623fc90f";
  };

  metals = (import "${derivations}/metals/default.nix") {
    inherit pkgs jdk;
  };

  nvim = (import "${derivations}/nvim/scala/default.nix") {
    inherit pkgs metals;
  };

  sbt = (import "${derivations}/sbt/default.nix") {
    inherit pkgs jdk;
  };

in

pkgs.mkShell {

  nativeBuildInputs = [ sbt ] ++ (if builtins.getEnv "CI" == "true" then [ ] else [ nvim ]);

  shellHook = ''
    sbt-test() {
      sbt \
        javafmtCheckAll \
        scalafmtCheckAll \
        "scalafixAll --check" \
        +test \
        publishLocal \
        +scripted
    }

    sbt-fmt() {
      sbt \
        scalafixAll \
        javafmtAll \
        scalafmtAll \
        scalafmtSbt
    }
  '';
}
