#!/bin/sh

set -e
cd "$(dirname "$0")"

./gradlew :app:jsBrowserDevelopmentExecutableDistribution
./extras/service__make.py
[ -e app/build/dist/js/developmentExecutable/app.js.map ] && rm app/build/dist/js/developmentExecutable/app.js.map

[ "$1" = "--open" ] && open "file://$(pwd)/app/build/dist/js/developmentExecutable/index.html"
