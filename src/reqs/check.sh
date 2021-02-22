#!/usr/bin/env bash

set -e

for f in $(dirname $0)/*
do
  if [ -d $f ]
  then
    pushd $f
    sbt jetty:test
    popd
  fi
done
