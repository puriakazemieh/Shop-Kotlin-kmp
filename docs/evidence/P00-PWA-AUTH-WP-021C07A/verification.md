# P00-PWA-AUTH-WP-021C07A — guarded session-route activation

Date: 2026-10-25. The new adapter has no default activation path. It registers browser routes only with explicit server constants, a validated policy, a verified current session-schema marker and a non-placeholder server secret. Invalid or absent configuration returns no adapter, so the routes are not registered.

Disposable WordPress 6.4.3/PHP 8.1.27 against the isolated MariaDB fixture passed 10 checks. The suite verified the four registered paths, positive password login, rejected foreign origin, normalized API error response, `Cache-Control: no-store`, credential-free JSON, refresh, logout and unchanged mobile-controller availability. Docker PHP lint passed for the adapter, standalone bootloader and fixture. The retained local WordPress instance also returned HTTP 404 for `POST /wp-json/carmilla/v1/api/auth/web/login`, proving that this source change did not activate browser routes there.

The test does not expose a host port, persist a synthetic account, or deliver mail. It does not prove a real browser preserves cookies, ingress retains duplicate headers, or recovery routes work; those are deliberately deferred to C07B/C07C.
