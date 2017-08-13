# Changelog

## 3.0.0

* Add `containerScale` to allow forking of many instances of the container
* Make `containerLaunchCmd` dependent on `containerPort` and *webapp* path
* Make `debugOptions` dependent on `debugPort`

## 2.3.1

* Minor documentation updates and code refactoring

## 2.3.0

* Add `<container>:quickstart` that skips webapp prep
  ([#319](https://github.com/earldouglas/xsbt-web-plugin/pull/319))

## 2.1.0

* Cache library deps and project artifacts
  ([#284](https://github.com/earldouglas/xsbt-web-plugin/pull/284))

## 2.0.5

* Add `<container>:join` to block sbt on running container
  ([#280](https://github.com/earldouglas/xsbt-web-plugin/pull/280))

## 2.0.4

* Disable auto-triggering of the plugins ([#276](https://github.com/earldouglas/xsbt-web-plugin/pull/276))

## 2.0.3

* Fix the wrong-package *.war* file publishing ([#275](https://github.com/earldouglas/xsbt-web-plugin/pull/275))

## 2.0.2

* Re-enable *.war* file publishing

## 2.0.1

* Switch `containerLaunchCmd` from a setting to a task

## 2.0.0

* Use AutoPlugin everywhere
* Apply sbt plugin best practices
* Allow exit hook to be disabled
* Improve resource change detection
* Various cleanup and optimization

## 1.1.1

* Rename some task keys to less conflicty values

## 1.1.0

* Add a Jetty-based AutoPlugin
* AutoPlugin configuration via sbt settings
* Silence noisy JVM fork destruction
* Update to sbt 0.13.7
* Update to Scala 2.10.5
* Various documentation improvements

## 1.0.0

* Complete rewrite of xsbt-web-plugin
* Configuration is cleaner and more uniform
* Containers are always launched in forked JVMs
* Containers are run from community/third party libraries
* Container JVMs are configured independently of sbt JVM
* Artifacts are published to Bintray
* Tests are more comprehensive
* Internals are (hopefully) cleaner and faster to maintain

## 0.9.0

* Add a blocking container:launch command ([#152](https://github.com/earldouglas/xsbt-web-plugin/issues/152))
* Apply useFileMappedBuffer=false only under MS Windows ([#156](https://github.com/earldouglas/xsbt-web-plugin/issues/156))
* Order the Jetty 9 configurations per the 9.1 spec ([#157](https://github.com/earldouglas/xsbt-web-plugin/issues/157))
* Remove unused references to deprecated TagLibConfiguration ([#158](https://github.com/earldouglas/xsbt-web-plugin/issues/158))

## 0.8.0

* Add package-webapp task to create the contents of target/webapp ([129](https://github.com/earldouglas/xsbt-web-plugin/issues/129), [earldouglas](https://github.com/earldouglas))
* Alter warPostProcess to include the war target directory path ([139](https://github.com/earldouglas/xsbt-web-plugin/issues/139), [earldouglas](https://github.com/earldouglas))
* Various documentation improvements ([wiki](https://github.com/earldouglas/xsbt-web-plugin/wiki), [earldouglas](https://github.com/earldouglas)) 

## 0.7.0

* Add Tomcat section to the readme ([b04c261532](https://github.com/earldouglas/xsbt-web-plugin/commit/b04c261532), [earldouglas](https://github.com/earldouglas))
* Add a new test case for annotation support ([b66fc80afd](https://github.com/earldouglas/xsbt-web-plugin/commit/b66fc80afd), [earldouglas](https://github.com/earldouglas))
* Silence the misleading logs ([2d352140eb](https://github.com/earldouglas/xsbt-web-plugin/commit/2d352140eb), [earldouglas](https://github.com/earldouglas))
* Add container:test to start container, test, and stop container ([a86634fac7](https://github.com/earldouglas/xsbt-web-plugin/commit/a86634fac7), [earldouglas](https://github.com/earldouglas))
* Allow jetty-annotations to be omitted from libraryDependencies ([be1134d0bc](https://github.com/earldouglas/xsbt-web-plugin/commit/be1134d0bc), [earldouglas](https://github.com/earldouglas))
* Fix test dependencies ([3b407a3b08](https://github.com/earldouglas/xsbt-web-plugin/commit/3b407a3b08), [ustinov](https://github.com/ustinov))
* Fix small typo ([ad3e917dc7](https://github.com/earldouglas/xsbt-web-plugin/commit/ad3e917dc7), [ustinov](https://github.com/ustinov))
* Add annotations support ([9dddb9c3b8](https://github.com/earldouglas/xsbt-web-plugin/commit/9dddb9c3b8), [ustinov](https://github.com/ustinov))
* Fixed Tomcat Runner so it more closely matches what the Jetty Runner is doing ([f39ef2d3de](https://github.com/earldouglas/xsbt-web-plugin/commit/f39ef2d3de), [cdow](https://github.com/cdow))
* Remove SBT 0.12 support from scripted tests ([1b57a3a3df](https://github.com/earldouglas/xsbt-web-plugin/commit/1b57a3a3df), [cdow](https://github.com/cdow))
* Use default incOptions ([af0af36eb3](https://github.com/earldouglas/xsbt-web-plugin/commit/af0af36eb3), [earldouglas](https://github.com/earldouglas))
* Clean before testing, for good measure ([ce45b00121](https://github.com/earldouglas/xsbt-web-plugin/commit/ce45b00121), [earldouglas](https://github.com/earldouglas))
* Switch build status badge to Travis ([c75fb81c53](https://github.com/earldouglas/xsbt-web-plugin/commit/c75fb81c53), [earldouglas](https://github.com/earldouglas))
* Add travis-ci configuration ([744619ad0d](https://github.com/earldouglas/xsbt-web-plugin/commit/744619ad0d), [earldouglas](https://github.com/earldouglas))
* Clean up the logic around classloading  ([7769145ee9](https://github.com/earldouglas/xsbt-web-plugin/commit/7769145ee9), [cdow](https://github.com/cdow))

## 0.6.0

### Enhancements

* Add `host` setting for binding to a specific network interface ([ec26e5](https://github.com/JamesEarlDouglas/xsbt-web-plugin/commit/ec26e584a34c1159493d0bee2f68f595b6f02466))

### Other

* Move to sbt 0.13.1 and Scala 2.10.3

## 0.5.0

### Improvements

* Handle non-jar classpath components ([#126](https://github.com/JamesEarlDouglas/xsbt-web-plugin/pull/126))
* Fix tab completion for contexts ([#130](https://github.com/JamesEarlDouglas/xsbt-web-plugin/pull/130))
* Avoid file locking problem on windows set useFileMappedBuffer to false for jetty ([#134](https://github.com/JamesEarlDouglas/xsbt-web-plugin/pull/134))
* Fix for restart functionality ([3e1ddc](https://github.com/JamesEarlDouglas/xsbt-web-plugin/commit/3e1ddcd51673c729e225994185c7673ab2085128))
* Add support for jetty 9.1 ([83250d](https://github.com/JamesEarlDouglas/xsbt-web-plugin/commit/83250d45bb308f9952f74fb0d1565d6065d0106e))

Thanks to [jannic](https://github.com/jannic), [agjini](https://github.com/agjini), and [cdow](https://github.com/cdow)!

## 0.4.2

### Improvements

* Fix Scala binary version compatibility issue with Tomcat container ([994cfe](https://github.com/JamesEarlDouglas/xsbt-web-plugin/commit/994cfef669e21ffaaf9dbd0e428bcd34739182d4))
* Remove Tomcat's automatic reloading ([d7b062](https://github.com/JamesEarlDouglas/xsbt-web-plugin/commit/d7b062f92a5c626d3e94c71fa5f2a44415813297))

Thanks to [cdow](https://github.com/cdow)'s detective work!

## 0.4.1

### New features

* sbt 0.13.0 support

## 0.4.0

### New features

* sbt 0.13.0-RC4 support
* Jetty 9 container support

## 0.3.0

### New features

* Classes and resources can be packaged as a JAR file under *WEB_INF/lib*

## 0.2.13

### New features

* Cusomizable classpath in the webapp package
* Tomcat `baseDir` uses a temporary directory
* Code cleanup/simplification

## 0.2.12

### New features

* Resources are copied in the same order that the JVM resolves resources
* Tomcat 7 is supported as a container
* Versioning follows the common SBT plugin pattern
* `ssl` is a more configurable `TaskKey`

