#!/usr/bin/env bash

set -e

pushd `dirname $0`

function test() {
  # Note Scala 2.12.0 is not supported:
  # /tmp/sbt_10cf059/scala/ZincCompat.scala:19: error: type PlainNioFile is not a member of package scala.reflect.io
  #   type PlainNioFile = scala.reflect.io.PlainNioFile
  for SCALA_VERSION in 2.10.2 2.10.7 2.11.0 2.11.12 2.12.1 2.12.15 2.13.0 2.13.6
  do
    _test
  done
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
for SBT_VERSION in 0.13.6 0.13.18
do
  SBT_LAUNCH=https://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/$SBT_VERSION/sbt-launch.jar
  test
done

# Note sbt versions 1.0-1.2 are not supported:
# [error] java.lang.NoClassDefFoundError: sbt/State$StateOpsImpl$
# [error]         at com.earldouglas.xwp.ContainerPlugin$.$anonfun$onLoadSetting$2(ContainerPlugin.scala:178)

# sbt 1.3, 1.4
for SBT_VERSION in 1.3.0 1.3.13 1.4.0 1.4.9
do
  SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/$SBT_VERSION/sbt-launch-$SBT_VERSION.jar
  test
done

# sbt 1.5
# Scala 3 support was introduced in sbt 1.5.0
for SBT_VERSION in 1.5.0 1.5.7 1.6.0-RC2
do
  SCALA_VERSION=3.0.0 SBT_LAUNCH=https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/$SBT_VERSION/sbt-launch-$SBT_VERSION.jar
  _test
done

popd
