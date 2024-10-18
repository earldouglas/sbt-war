# Contributing

## Architecture

```mermaid
flowchart TB

    subgraph webapp
        WebappComponentsPlugin
        WebappComponentsRunnerPlugin
        webappRunnerCA[components-aware webapp-runner]
    end

    subgraph war
        WarPackagePlugin
        WarPackageRunnerPlugin
        webappRunner[webapp-runner]
    end

    SbtWar

    subgraph project
        resources
        classes
        lib
    end


    WebappComponentsPlugin-->resources
    WebappComponentsPlugin-->classes
    WebappComponentsPlugin-->lib

    WebappComponentsRunnerPlugin-->WebappComponentsPlugin
    WebappComponentsRunnerPlugin-->webappRunnerCA

    WarPackagePlugin-->WebappComponentsPlugin

    WarPackageRunnerPlugin-->WarPackagePlugin
    WarPackageRunnerPlugin-->webappRunner

    SbtWar-->WebappComponentsRunnerPlugin
    SbtWar-->WarPackageRunnerPlugin
```


## Testing

```
$ sbt test scripted
```

## Publishing

sbt-war uses the process outlined in the [Using
Sonatype](https://www.scala-sbt.org/release/docs/Using-Sonatype.html)
section of the sbt manual for publishing to Maven Central via Sonatype.

Create a staging release in Sonatype:

```
$ nix-shell
$ sbt
> set ThisBuild / version := "5.0.0-M4"
> publishSigned
```

Review it:

* Go to [Staging
  Repositories](https://oss.sonatype.org/#stagingRepositories) on Nexus
  Repository Manager
* Review the contents of the staging repository


Release it:

* Close the staging repository
* Release the staging repository to promote to Maven Central

Wait for it to be synced to Maven Central:

* <https://repo1.maven.org/maven2/com/earldouglas/sbt-war_2.12_1.0/>

Update the documentation:

```
$ git checkout -b v5.0.0-M4
$ sed -i 's/4\.2\.2/5.0.0-M4/g' README.md
$ git add README.md
$ git commit -m "Update version to 5.0.0-M4"
$ git push origin v5.0.0-M4
```

Tag the release:

```
$ git tag 5.0.0-M4
$ git push --tags origin
```

Update the [Giter8 template](https://github.com/earldouglas/sbt-war.g8)
to use the new version.
