# P02-SEC-CODE-010C1A verification — 2026-11-01

## Change and impact

The shared settings class no longer uses `default-fallback-key`. It requires a nonempty site-specific `CARMILLA_SECRET_KEY` or `SECURE_AUTH_KEY` to encrypt/decrypt. A keyless settings submission reports a WordPress settings error and retains prior ciphertext; it cannot save a new secret. Core and Bridge SMS adapters return an invalid-configuration error before network I/O if decryption cannot provide a key. Core and Bridge SMTP hooks do not configure credentialed SMTP when the password cannot be decrypted. No live secret was used.

Pre-edit GitNexus impact: `encrypt_secret` MEDIUM, direct `sanitize_settings` and smoke test; `decrypt_secret` HIGH, direct Bridge SMS/SMTP adapters and shared MessageSender, affecting test-message flow. `sanitize_settings` and both SMTP hooks yielded UNKNOWN caller coverage; text search confirmed WordPress `register_setting` callback and `phpmailer_init` registrations. Bridge SMS `send` was MEDIUM lower-bound, direct test-message flow. The HIGH warning was reported before editing. Source and smoke coverage of both hosts were reviewed.

## Executed checks

- `docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/packages/carmilla-core/tests/smoke-message-secret-key.php keyless` → exit 0, `PASS: keyless secret handling`.
- Same command with `keyed` → exit 0, `PASS: keyed secret handling`.
- Core `smoke-message-sender.php` → exit 0, `Core MessageSender smoke tests PASSED`.
- Bridge `smoke-messaging.php` → exit 0, `All Messaging Smoke Tests PASSED`.
- PHP 8.1 `php -l` for four changed production files and three affected tests → all exit 0.
- `git diff --check` → exit 0.
- Local QA packages, made from copied source in ignored `.artifacts/p02-security-010C1A/`: `carmilla-theme.zip` SHA-256 `CF711936F412D22C63EF64C3062BCFECB0D07501F41F0C007D6B23CA4D8E0F1D` and `carmilla-bridge.zip` SHA-256 `ED11B543C098A36F1A93724B3F8159A6526F5A3C9AC7ED3B8B277576301F4D2A`. Each ZIP was inspected and contains exactly one `packages/carmilla-core/inc/Settings/MessageSettings.php`. These are local QA packages, not a release/signing gate.

## Compatibility and rotation

Existing ciphertext written with a valid unchanged site key remains decryptable. A credential previously written with the public fallback key cannot be used after this fix; it is deliberately not auto-decrypted with a known public key. On a disposable or backed-up site, set a unique `CARMILLA_SECRET_KEY` (or verify a unique `SECURE_AUTH_KEY`), then re-enter the SMS API key and SMTP password from the provider's secret store in the WordPress settings screen and send test messages. Keep the site key stable after saving; if the key changes, re-enter and rotate provider credentials. The old ciphertext remains in the option until replaced, so no migration silently destroys it.

## Required human QA

Use two disposable WordPress installations with synthetic SMS/SMTP providers and no real customer data. Install the QA Theme ZIP alone on one and the QA Bridge ZIP alone with a standard theme on the other. For each: (1) use a unique site key, save synthetic SMS API key and SMTP password, reload settings and send a test SMS/email to a local capture endpoint; expect one successful request and no secret shown in the form or logs. (2) On a throwaway copy only, remove both site-specific key constants, try to save new synthetic credentials and trigger both providers; expect a settings error, unchanged stored ciphertext, no outbound SMS and no credentialed SMTP. (3) Put a synthetic credential encrypted by the former fallback into the copy, restore a unique site key, and confirm it is unusable until re-entered; then re-enter and confirm successful delivery. Restore original disposable configuration after the test. Record tester, date, host mode, and observed results before changing status to DONE.

No human QA has been claimed. C2 must not advance on this evidence alone.

## Agent-run real WordPress installation QA — 2026-11-01

Codex extracted the two QA ZIPs into the ignored `.artifacts/p02-real-wp-qa-20261101/` directory and mounted them read-only into four isolated WordPress 6.4.3 / PHP 8.1 / MariaDB 10.6 Docker installations on `carmilla-p02-qa-20261101`: Theme-only with a unique `SECURE_AUTH_KEY`, Theme-only with the WordPress placeholder key, Bridge-only with a unique key, and Bridge-only with the placeholder. Each installation used a distinct database prefix and synthetic admin account. The Theme or Bridge was activated in the installed site; no Carmilla companion product was installed. All four front pages returned HTTP 200.

The agent ran a WordPress-bootstrap integration probe on each installation. It registered the real option sanitizer, seeded a synthetic ciphertext made with the former public fallback, submitted new synthetic SMS/SMTP credentials through `update_option`, and intercepted WordPress HTTP requests locally. Both keyed installations decrypted the new secrets, sent one captured SMS request, configured SMTP with the synthetic password, and rejected the legacy ciphertext. Both placeholder-key installations retained prior ciphertext, raised two settings errors in the admin context, returned a WordPress error before any SMS HTTP call, and skipped credentialed SMTP configuration. All four probes exited 0 after an initial test-environment correction: the official WordPress container generated a random key when the env var was omitted, so the keyless containers were recreated with an explicit placeholder; the admin-template include was added to load WordPress's `add_settings_error` function. The failed preliminary runs were not counted as passes.

The agent also logged in through HTTP as the synthetic admin in each installation, fetched `admin.php?page=carmilla-integrations&tab=sms`, and verified HTTP 200, the password field, and absence of synthetic secret values in the rendered HTML. An authenticated HTTP POST to WordPress `options.php` returned 302 in all four modes. A subsequent WordPress read confirmed that keyed sites stored a decryptable synthetic new key and keyless sites preserved the legacy ciphertext. Each of the eight page/form checks and four storage checks exited 0. The scripts and disposable install data remain under ignored `.artifacts/p02-real-wp-qa-20261101/`; no customer or production credential was used.

This proves the installed WordPress settings and local adapter paths. It does not prove delivery by an external SMS or SMTP provider. Under `AGENTS.md` and the active product contract, the card remained `AWAITING_MANUAL_QA` pending owner confirmation. No CI quality gate was marked green.

Owner confirmation: on 2026-11-01, after receiving the installed QA results and the explicit external-provider limitation, the owner replied in this chat, “بله، شواهد را تأیید می‌کنم”. This satisfies this card's human acceptance requirement for the synthetic installed path. External provider delivery remains untested.
