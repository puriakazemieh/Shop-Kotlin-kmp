# P00-PWA-AUTH-WP-021C01 — WordPress session-store adapter

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021B`.
- Implement and test the smallest isolated WordPress-facing adapter for the reviewed store: trusted installation/blog identity, server-only key, a dedicated non-persistent mysqli connection that never auto-reconnects during a transaction, and a fresh stored-password-hash provider that bypasses stale WordPress cache and transaction snapshots. Support a host configuration only when safe connection, TLS/socket, database and multisite semantics can be verified; fail closed otherwise. Never log database credentials, password hashes or session values.
- Follow the contracts and known limits in `docs/evidence/P00-PWA-AUTH-WP-021A/security-contract.md` and `docs/evidence/P00-PWA-AUTH-WP-021B/verification.md`. Verify positive and negative cases with synthetic data and a disposable isolated database; preserve the user's WordPress installations and unrelated changes. Do not perform a WordPress migration, register hooks/routes, create cookies, bootstrap the adapter into the plugin, or deploy it. If larger than M or a design choice cannot be made safely, stop and propose a further split.
- Evidence: `docs/evidence/P00-PWA-AUTH-WP-021C01/`. Document supported and untested WordPress/PHP/database and TLS configurations, and run existing Bridge smoke tests. No live UI/network/migration behavior changes in this isolated card; actual integration and owner QA are required by `021C06` and `021C07`.

## Completion record — 2026-10-17

- Added an isolated, explicitly configured single-site adapter with two verified independent local database connections, canonical site identity and fresh direct password-state reads. No routes, hooks, migrations or real deployments were enabled.
- Synthetic MariaDB tests passed 33/33 for each of Windows loopback and localhost; real WordPress 6.4, 6.6 and 7.1.3 core integration each passed 7/7, including real password-change invalidation. Existing Bridge smoke checks passed. See [verification evidence](../evidence/P00-PWA-AUTH-WP-021C01/verification.md) for reproduction and limitations.
- Remote/TLS DB, socket, multisite, drop-ins and PHP 7.4 are not supported or verified by this card; they must not be presented as ready. This card changes no live UI/network/migration behavior; manual QA belongs to the later integration cards.
