#!/bin/sh

set -e
cd "$(dirname "$0")"

./gradlew :app:jsBrowserDevelopmentWebpack
[ "$1" = "--open" ] && open "file://$(pwd)/app/build/dist/js/developmentExecutable/index.html"
