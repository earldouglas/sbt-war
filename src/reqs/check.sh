#!/usr/bin/env bash

set -e

pushd `dirname $0`

function test() {
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

SCALA_VERSION=2.10.2 SBT_VERSION=0.13.6 SBT_LAUNCH=https://dl.bintray.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.6/sbt-launch.jar test
SCALA_VERSION=2.10.2 SBT_VERSION=0.13.18 SBT_LAUNCH=https://dl.bintray.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.18/sbt-launch.jar test
SCALA_VERSION=2.13.4 SBT_VERSION=1.3.0 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.3.0/sbt-launch-1.3.0.jar test
SCALA_VERSION=2.13.4 SBT_VERSION=1.5.2 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.5.2/sbt-launch-1.5.2.jar test

popd
