#!/bin/bash
cd $(dirname $0)
if [ -z "$1" ]; then
		echo "Usage: $0 <plugin version>"
		exit
fi
PLUGIN_VERSION=$1
for f in $(find src/sbt-test -path '*plugins/build.sbt'); do
		echo "libraryDependencies += \"com.github.siasia\" %% \"xsbt-web-plugin\" % \"$PLUGIN_VERSION\"" > $f
done