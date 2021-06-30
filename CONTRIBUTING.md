# Contributing

## Publishing

xsbt-web-plugin uses the process outlined in the [Using Sonatype][1]
section of the sbt manual for publishing to Maven Central via Sonatype.

[1]: https://www.scala-sbt.org/release/docs/Using-Sonatype.html

Create a staging release in Sonatype:

```
$ sbt
> set version := "4.2.3"
> ^publishSigned
```

Review it:

* [ ] Go to [Staging Repositories][2] on Nexus Repository Manager
* [ ] Review the contents of the staging repository

[2]: https://oss.sonatype.org/#stagingRepositories

Release it:

* [ ] Close the staging repository
* [ ] Release the staging repository to promote to Maven Central

Wait for it to be synced to Maven Central:

* <https://repo1.maven.org/maven2/com/earldouglas/xsbt-web-plugin_2.10_0.13/4.2.2/>
* <https://repo1.maven.org/maven2/com/earldouglas/xsbt-web-plugin_2.12_1.0/4.2.2/>

Update the documentation:

```
$ git checkout -b v4.2.3
$ sed -i 's/4\.2\.2/4.2.3/g' README.md
$ git add README.md
$ git commit -m "Update version to 4.2.3"
$ git push origin v4.2.3
```

Tag the release:

```
$ git tag 4.2.3
$ git push --tags origin
```

Update the [Giter8 template][3] to use the new version.

[3]: https://github.com/earldouglas/xsbt-web-plugin.g8
