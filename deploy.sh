#!/bin/bash
if [[ ! $1 ]]; then
		echo "Please provide artifact version"
else
VERSION=$1
TARGET=target/scala-2.9.1/sbt-0.11.0/xsbt-web-plugin-
POM=${TARGET}${VERSION}.pom

function mvn-gpg {
		if [[ $2 ]]; then
				CLASSIFIER=-Dclassifier=$2
		fi
		mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=${POM} -Dfile=${TARGET}${VERSION}$1 $CLASSIFIER
}

mvn-gpg .jar
mvn-gpg -sources.jar sources
mvn-gpg -javadoc.jar javadoc
fi