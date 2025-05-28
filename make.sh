#!/bin/bash

set -e
cd "$(dirname "$0")"

if [[ -e features/app/src/jsMain/resources/index.html ]] ; then
    BUILD=$(cat features/app/src/jsMain/resources/index.html | grep bpe-build | sed -E 's/^.*bpe\-build="([0-9]*)".*$/\1/')
    BUILD=$((BUILD + 1))
    sed -i.bak -E 's/bpe\-build="[0-9]*"/bpe-build="'"$BUILD"'"/' features/app/src/jsMain/resources/index.html
    rm features/app/src/jsMain/resources/index.html.bak
fi

./gradlew :features:app:jsBrowserDevelopmentExecutableDistribution
./extras/service__make.py

[[ -e features/app/build/dist/js/developmentExecutable/app.js.map ]] \
    && rm features/app/build/dist/js/developmentExecutable/app.js.map

[[ "$1" = "--open" ]] && open "file://$(pwd)/features/app/build/dist/js/developmentExecutable/index.html"
