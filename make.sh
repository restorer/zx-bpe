#!/bin/bash

set -e
cd "$(dirname "$0")"

if [[ -e app/src/jsMain/resources/index.html ]] ; then
    BUILD=$(cat app/src/jsMain/resources/index.html | grep bpe-build | sed -E 's/^.*bpe\-build="([0-9]*)".*$/\1/')
    BUILD=$((BUILD + 1))
    sed -i.bak -E 's/bpe\-build="[0-9]*"/bpe-build="'"$BUILD"'"/' app/src/jsMain/resources/index.html
    rm app/src/jsMain/resources/index.html.bak
fi

./gradlew :app:jsBrowserDevelopmentExecutableDistribution
./extras/service__make.py
[[ -e app/build/dist/js/developmentExecutable/app.js.map ]] && rm app/build/dist/js/developmentExecutable/app.js.map

[[ "$1" = "--open" ]] && open "file://$(pwd)/app/build/dist/js/developmentExecutable/index.html"
