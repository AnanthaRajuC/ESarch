#!/usr/bin/env bash

set -e +x

pushd source-code
  echo "Testing and Packaging the trader App JAR..."
  cd trader-app
  ../mvnw verify
popd

jar_count=`find source-code/trader-app/target -type f -name *.jar | wc -l`

if [ $jar_count -gt 1 ]; then
  echo "More than one jar found, don't know which one to deploy. Exiting :("
  exit 1
fi

find source-code/trader-app/target -type f -name *.jar -exec cp "{}" package-output/trader-app.jar \;

echo "Done packaging"
exit 0