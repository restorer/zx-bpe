"use strict";

const CACHE_NAME = "v1";

const ASSETS = [
    "./",
    "./app.js",
    "./favicon.ico",
    "./index.html",
    "./service.js",
    "./manifest.json",
    "./drawable/shape__stroke_box.svg",
    "./drawable/selection__flip_vertical.svg",
    "./drawable/layer__locked.svg",
    "./drawable/palette__char.svg",
    "./drawable/shape__line.svg",
    "./drawable/light__transparent.svg",
    "./drawable/layer__move_up.svg",
    "./drawable/layer__move_down.svg",
    "./drawable/shape__stroke_ellipse.svg",
    "./drawable/shape__fill_box.svg",
    "./drawable/tool__paint.svg",
    "./drawable/layer__merge.svg",
    "./drawable/menu__selection.svg",
    "./drawable/layer__delete.svg",
    "./drawable/palette__ink.svg",
    "./drawable/selection__copy.svg",
    "./drawable/layer__visible.svg",
    "./drawable/layer__invisible.svg",
    "./drawable/layers.svg",
    "./drawable/palette__paper.svg",
    "./drawable/layer__unlocked.svg",
    "./drawable/menu__primary.svg",
    "./drawable/tool__pick_color.svg",
    "./drawable/palette__flash.svg",
    "./drawable/layer__unmasked.svg",
    "./drawable/selection__rotate_cw.svg",
    "./drawable/palette__bright.svg",
    "./drawable/toolbox__undo.svg",
    "./drawable/tool__erase.svg",
    "./drawable/selection__paste.svg",
    "./drawable/type__qblock.svg",
    "./drawable/toolbox__mode_edge.svg",
    "./drawable/selection__rotate_ccw.svg",
    "./drawable/tool__select.svg",
    "./drawable/selection__cut.svg",
    "./drawable/light__on.svg",
    "./drawable/palette__color.svg",
    "./drawable/toolbox__mode_center.svg",
    "./drawable/type__vblock.svg",
    "./drawable/shape__point.svg",
    "./drawable/toolbox__redo.svg",
    "./drawable/selection__flip_horizontal.svg",
    "./drawable/layer__masked.svg",
    "./drawable/shape__fill_ellipse.svg",
    "./drawable/layer__convert.svg",
    "./drawable/type__scii.svg",
    "./drawable/layer__create.svg",
    "./drawable/type__hblock.svg",
    "./drawable/light__off.svg",
    "./drawable/specscii__24x24.png",
    "./drawable/light__force_transparent.svg",
    "./style/bpe.css",
    "./style/material.css",
    "./style/specscii.css",
    "./icons/icon__48x48.png",
    "./icons/icon__152x152.png",
    "./icons/icon__72x72.png",
    "./icons/icon__96x96.png",
    "./icons/icon__384x384.png",
    "./icons/icon__192x192.png",
    "./icons/icon__128x128.png",
    "./icons/icon__144x144.png",
    "./icons/icon__512x512.png",
    "./icons/icon__168x168.png"
];

async function executeInstall() {
    const cache = await caches.open(CACHE_NAME);
    return cache.addAll(ASSETS);
}

async function executeFetch(request) {
    const url = new URL(request.url);

    if (url.pathname.endsWith(".bpe")) {
        return null;
    }

    const fetchPromise = fetch(request)
        .then(async (response) => {
            if (response.ok) {
                const cache = await caches.open(CACHE_NAME);
                await cache.put(url.href, response.clone());

                return response;
            }

            return await caches.match(url.href);
        })
        .catch(() => null);

    return (await caches.match(url.href)) || (await fetchPromise);
}

self.addEventListener("install", (event) => {
    event.waitUntil(executeInstall());
});

self.addEventListener("fetch", (event) => {
    const response = executeFetch(event.request);

    if (response) {
        event.respondWith(response);
    }
});
