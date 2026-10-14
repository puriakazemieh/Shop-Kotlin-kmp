# P00-T02 — Current identifier and artifact inventory

- **Date:** 2026-10-14
- **Reviewer:** Codex, source and local QA inventory review
- **Build fingerprint:** `21bf9fed`
- **Result:** PASS for the approved P00–P10 scope

## Approved release-test scope

This verification cycle ships the WordPress shop-only path and its PWA.  It
does not ship an Android, iOS, desktop, or Spring artifact.  Therefore no
mobile-store identity, signing key, or mobile binary is an input to this
cycle's release gate.

## Source identifiers found

| Surface | Identifier / endpoint | Status |
| --- | --- | --- |
| Android (historical inventory) | `com.kazemieh.shop` | Found in `androidApp/build.gradle.kts`; out of current scope |
| Android flavour suffixes (historical inventory) | `atris`, `chronos`, `academy`, `psych`, `wp` | Found in `androidApp/build.gradle.kts`; out of current scope |
| iOS (historical inventory) | `com.kazemieh.shop.kmpShop` | Recorded in `docs/inventory/app-identity.md`; out of current scope |
| Web/PWA QA host | `http://localhost/carmilla-pwa-qa/` | Local QA only |
| WordPress QA API | `http://localhost/carmilla-p07-qa/wp-json/carmilla/v1/` | Active local QA configuration |
| Public release domain | Not assigned | No production domain is claimed by this cycle |

## Current installable QA artifacts

| Artifact | Version | Purpose |
| --- | --- | --- |
| `wordpress/carmilla-theme.zip` | `0.8.0` | Theme QA artifact |
| `wordpress/carmilla-bridge.zip` | `1.0.0` | Bridge QA artifact |

There is no Git release tag and no APK/AAB/IPA in the approved scope. Older
ZIP files remain historical references and are not the artifact under test.

## Explicitly unresolved, but not a gate for this scope

The repository contains no Android/iOS signing owner or marketplace-account
record. Their owner remains `UNKNOWN` until a future mobile release is in
scope. No secret, keystore, account credential, customer data, or production
endpoint was inspected or recorded.
