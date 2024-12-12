"use strict";

const CACHE_NAME = "v1";

const ASSETS = [
    "/bpe",
    "/bpe/",
    "/bpe/favicon.ico",
    "/bpe/index.html",
    "/bpe/service.js",
    "/bpe/manifest.json",
    "/bpe/drawable/mode__center.svg",
    "/bpe/drawable/shape__stroke_box.svg",
    "/bpe/drawable/selection__flip_vertical.svg",
    "/bpe/drawable/layer__locked.svg",
    "/bpe/drawable/palette__char.svg",
    "/bpe/drawable/shape__line.svg",
    "/bpe/drawable/layer__move_up.svg",
    "/bpe/drawable/layer__move_down.svg",
    "/bpe/drawable/shape__stroke_ellipse.svg",
    "/bpe/drawable/shape__fill_box.svg",
    "/bpe/drawable/tool__paint.svg",
    "/bpe/drawable/layer__merge.svg",
    "/bpe/drawable/layer__delete.svg",
    "/bpe/drawable/toolbox__paste.svg",
    "/bpe/drawable/menu__secondary.svg",
    "/bpe/drawable/palette__ink.svg",
    "/bpe/drawable/selection__copy.svg",
    "/bpe/drawable/layer__visible.svg",
    "/bpe/drawable/layer__invisible.svg",
    "/bpe/drawable/layers.svg",
    "/bpe/drawable/palette__paper.svg",
    "/bpe/drawable/layer__unlocked.svg",
    "/bpe/drawable/menu__primary.svg",
    "/bpe/drawable/tool__pick_color.svg",
    "/bpe/drawable/palette__flash.svg",
    "/bpe/drawable/layer__unmasked.svg",
    "/bpe/drawable/selection__rotate_cw.svg",
    "/bpe/drawable/palette__bright.svg",
    "/bpe/drawable/toolbox__undo.svg",
    "/bpe/drawable/tool__erase.svg",
    "/bpe/drawable/type__qblock.svg",
    "/bpe/drawable/selection__rotate_ccw.svg",
    "/bpe/drawable/tool__select.svg",
    "/bpe/drawable/selection__cut.svg",
    "/bpe/drawable/palette__color.svg",
    "/bpe/drawable/type__vblock.svg",
    "/bpe/drawable/mode__edge.svg",
    "/bpe/drawable/shape__point.svg",
    "/bpe/drawable/toolbox__redo.svg",
    "/bpe/drawable/selection__flip_horizontal.svg",
    "/bpe/drawable/layer__masked.svg",
    "/bpe/drawable/shape__fill_ellipse.svg",
    "/bpe/drawable/layer__convert.svg",
    "/bpe/drawable/type__scii.svg",
    "/bpe/drawable/layer__create.svg",
    "/bpe/drawable/type__hblock.svg",
    "/bpe/drawable/specscii__24x24.png",
    "/bpe/style/../drawable/specscii__24x24.png",
    "/bpe/style/bpe.css",
    "/bpe/style/material.css",
    "/bpe/style/specscii.css",
    "/bpe/icons/icon__48x48.png",
    "/bpe/icons/icon__152x152.png",
    "/bpe/icons/icon__72x72.png",
    "/bpe/icons/icon__96x96.png",
    "/bpe/icons/icon__384x384.png",
    "/bpe/icons/icon__192x192.png",
    "/bpe/icons/icon__128x128.png",
    "/bpe/icons/icon__144x144.png",
    "/bpe/icons/icon__512x512.png",
    "/bpe/icons/icon__168x168.png"
];

async function executeInstall() {
    const cache = await caches.open(CACHE_NAME);
    return cache.addAll(ASSETS);
}

async function executeFetch(request) {
    if ((new URL(request.url)).pathname.endsWith(".bpe")) {
        return null;
    }

    const fetchPromise = fetch(request.clone()).then(async (response) => {
        if (response.ok) {
            const cache = await caches.open(CACHE_NAME);
            await cache.put(request, response.clone());
        }

        return response;
    });

    return (await caches.match(request.clone())) || (await fetchPromise);
}

self.addEventListener("install", (event) => {
    event.waitUntil(executeInstall());
});

self.addEventListener("fetch", (event) => {
    const response = executeFetch(event.request);

    if (response != null) {
        event.respondWith(response);
    }
});
