// Legacy entrypoint retained only so generated distributions never contain an
// unrestricted cache-all worker. The active PWA profiles register sw-wp.js or
// sw-spring.js instead.
self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', event => event.waitUntil(self.clients.claim()));
self.addEventListener('fetch', event => event.respondWith(fetch(event.request)));
