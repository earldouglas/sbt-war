#!/usr/bin/env bash

set -e

pushd `dirname $0`

function test() {
  SCALA_VERSION=2.10.2 _test
  SCALA_VERSION=2.10.7 _test

  SCALA_VERSION=2.11.0 _test
  SCALA_VERSION=2.11.12 _test

  SCALA_VERSION=2.12.0 _test
  SCALA_VERSION=2.12.13 _test

  SCALA_VERSION=2.13.0 _test
  SCALA_VERSION=2.13.6 _test
}

function _test() {
  TMP_SRC_DIR=`mktemp -d`
  cp -rf . $TMP_SRC_DIR

  pushd $TMP_SRC_DIR

  echo "scalaVersion := \"$SCALA_VERSION\"" > scalaVersion.sbt
  echo "sbt.version=$SBT_VERSION" > project/build.properties

  cat project/build.properties
  cat scalaVersion.sbt

  TMP_HOME_DIR=`mktemp -d`

  curl -s -L $SBT_LAUNCH -o $TMP_HOME_DIR/sbt-launch-$SBT_VERSION.jar

  HOME=$TMP_HOME_DIR java -jar $TMP_HOME_DIR/sbt-launch-$SBT_VERSION.jar clean jetty:test

  popd
}


# sbt 0.13
SBT_VERSION=0.13.6 SBT_LAUNCH=https://dl.bintray.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.6/sbt-launch.jar test
SBT_VERSION=0.13.18 SBT_LAUNCH=https://dl.bintray.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.18/sbt-launch.jar test

# sbt 1.0
SBT_VERSION=1.0.0 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.0.0/sbt-launch-1.0.0.jar test
SBT_VERSION=1.0.4 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.0.4/sbt-launch-1.0.4.jar test

# sbt 1.1
SBT_VERSION=1.1.0 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.1.0/sbt-launch-1.1.0.jar test
SBT_VERSION=1.1.6 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.1.6/sbt-launch-1.1.6.jar test

# sbt 1.2
SBT_VERSION=1.2.0 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.2.0/sbt-launch-1.2.0.jar test
SBT_VERSION=1.2.8 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.2.8/sbt-launch-1.2.8.jar test

# sbt 1.3
SBT_VERSION=1.3.0 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.3.0/sbt-launch-1.3.0.jar test
SBT_VERSION=1.3.13 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.3.13/sbt-launch-1.3.13.jar test

# sbt 1.4
SBT_VERSION=1.4.0 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.4.0/sbt-launch-1.4.0.jar test
SBT_VERSION=1.4.9 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.4.9/sbt-launch-1.4.9.jar test

# sbt 1.5 (note Scala 3 support was introduced in sbt 1.5.0)
SBT_VERSION=1.5.0 SCALA_VERSION=3.0.0 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.5.0/sbt-launch-1.5.0.jar _test
SBT_VERSION=1.5.2 SCALA_VERSION=3.0.0 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.5.2/sbt-launch-1.5.2.jar _test

popd
