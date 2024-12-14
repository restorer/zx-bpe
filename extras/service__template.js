"use strict";

const CACHE_NAME = "v1";

const ASSETS = [
/** ASSETS **/
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
