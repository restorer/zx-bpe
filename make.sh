#!/bin/bash

set -e
cd "$(dirname "$0")"

APP_PATH="features/app-pwa"
APP_MODULE=":features:app-pwa"
UI_PATH="features/ui-pwa-impl"

do_parse_opts () {
    while [[ $# -gt 0 ]] ; do
        case $1 in
            --open)
                HAS_OPEN=1
                ;;

            --prod)
                HAS_PROD=1
                ;;

            *)
                echo "Usage: $0 [--prod] [--open]"
                exit 1
                ;;
        esac

        shift
    done
}

do_patch_build_version () {
    local INDEX_PATH="$UI_PATH/src/jsMain/resources/index.html"
    [[ ! -e "$INDEX_PATH" ]] && return 0

    BUILD=$(grep bpe-build "$INDEX_PATH" | sed -E 's/^.*bpe\-build="([0-9]*)".*$/\1/')
    BUILD=$((BUILD + 1))

    sed -i.bak -E 's/bpe\-build="[0-9]*"/bpe-build="'"$BUILD"'"/' "$INDEX_PATH"
    rm "${INDEX_PATH}.bak"
}

do_build () {
    local GRADLE_OPTS=""
    [[ $HAS_PROD != "" ]] && GRADLE_OPTS="-Pbpe.production=true"

    if [[ $HAS_PROD != "" && ! -e build/bpe.prod ]] || [[ $HAS_PROD = "" && -e build/bpe.prod ]] ; then
        ./gradlew clean
        mkdir -p build

        if [[ $HAS_PROD != "" ]] ; then
            touch build/bpe.prod
        elif [[ -e build/bpe.prod ]] ; then
            rm build/bpe.prod
        fi
    fi

    ./gradlew $GRADLE_OPTS "${APP_MODULE}:jsBrowserDevelopmentExecutableDistribution"
    ./extras/service__make.py

    mkdir -p build/bpe
    find build/bpe -mindepth 1 -maxdepth 1 -exec rm -r {} \;

    cp -R "$UI_PATH/src/jsMain/resources/" build/bpe
    cp "$APP_PATH/build/dist/js/developmentExecutable/app-pwa.js" build/bpe/app.js
}

do_parse_opts "$@"
do_patch_build_version
do_build

[[ $HAS_OPEN != "" ]] && open "file://$(pwd)/build/bpe/index.html"
