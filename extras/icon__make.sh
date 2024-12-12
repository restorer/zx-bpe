#!/usr/bin/env bash

set -e
cd "$(dirname "$0")"

mkdir -p ../app/src/jsMain/resources/icons

magick icon.png -scale 48x48 ../app/src/jsMain/resources/icons/icon__48x48.png
magick icon.png -scale 72x72 ../app/src/jsMain/resources/icons/icon__72x72.png
magick icon.png -scale 96x96 ../app/src/jsMain/resources/icons/icon__96x96.png
magick icon.png -scale 128x128 ../app/src/jsMain/resources/icons/icon__128x128.png
magick icon.png -scale 144x144 ../app/src/jsMain/resources/icons/icon__144x144.png
magick icon.png -scale 152x152 ../app/src/jsMain/resources/icons/icon__152x152.png
magick icon.png -scale 168x168 ../app/src/jsMain/resources/icons/icon__168x168.png
magick icon.png -scale 192x192 ../app/src/jsMain/resources/icons/icon__192x192.png
magick icon.png -scale 384x384 ../app/src/jsMain/resources/icons/icon__384x384.png
magick icon.png -scale 512x512 ../app/src/jsMain/resources/icons/icon__512x512.png

magick icon.png \
    -bordercolor black \
    -border 0 \
    \( -clone 0 -filter box -resize 16x16 \) \
    \( -clone 0 -filter box -resize 32x32 \) \
    \( -clone 0 -filter box -resize 48x48 \) \
    \( -clone 0 -filter box -resize 64x64 \) \
    -delete 0 \
    -alpha off \
    -colors 256 \
    ../app/src/jsMain/resources/favicon.ico
