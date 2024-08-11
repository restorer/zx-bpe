#!/bin/sh

set -e
cd "$(dirname "$0")"

./gradlew :app:assemble
[ "$1" = "--open" ] && open "file://$(pwd)/app/build/dist/js/productionExecutable/index.html"
