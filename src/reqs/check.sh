#!/usr/bin/env bash

set -e

function test() {
  echo "scalaVersion := \"$SCALA_VERSION\"" > scalaVersion.sbt
  echo "sbt.version=$SBT_VERSION" > project/build.properties

  cat project/build.properties
  cat scalaVersion.sbt

  sbt \
    -Dsbt.global.base=`mktemp -d` \
    -Dsbt.boot.directory=`mktemp -d` \
    -Dsbt.ivy.home=`mktemp -d` \
    clean \
    jetty:test
}

SCALA_VERSION=2.10.2 SBT_VERSION=0.13.6 test
SCALA_VERSION=2.10.2 SBT_VERSION=0.13.18 test
SCALA_VERSION=2.13.4 SBT_VERSION=1.3.0 test
SCALA_VERSION=2.13.4 SBT_VERSION=1.5.0 test
