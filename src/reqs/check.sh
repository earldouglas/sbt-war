#!/usr/bin/env bash

set -e

function test() {
  echo "scalaVersion := \"$SCALA_VERSION\"" > scalaVersion.sbt
  echo "sbt.version=$SBT_VERSION" > project/build.properties

  cat project/build.properties
  cat scalaVersion.sbt

  curl -s -L $SBT_LAUNCH -o sbt-launch.jar

  HOME=`mktemp -d` java -jar sbt-launch.jar clean jetty:test
}

SCALA_VERSION=2.10.2 SBT_VERSION=0.13.6 SBT_LAUNCH=https://dl.bintray.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.6/sbt-launch.jar test
SCALA_VERSION=2.10.2 SBT_VERSION=0.13.18 SBT_LAUNCH=https://dl.bintray.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.18/sbt-launch.jar test
SCALA_VERSION=2.13.4 SBT_VERSION=1.3.0 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.3.0/sbt-launch-1.3.0.jar test
SCALA_VERSION=2.13.4 SBT_VERSION=1.5.0 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.5.0/sbt-launch-1.5.0.jar test
