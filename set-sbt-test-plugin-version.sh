#!/bin/bash
cd $(dirname $0)
if [ -z "$1" ]; then
		echo "Usage: $0 <plugin version>"
		exit
fi
PLUGIN_VERSION=$1
for group in $(ls src/sbt-test); do
		for test_dir in $(ls src/sbt-test/$group); do
				echo "addSbtPlugin(\"com.earldouglas\" % \"xsbt-web-plugin\" % \"$PLUGIN_VERSION\")" > src/sbt-test/$group/$test_dir/project/plugins.sbt
		done
done
