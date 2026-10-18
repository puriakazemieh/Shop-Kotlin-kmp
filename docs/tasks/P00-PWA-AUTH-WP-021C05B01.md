# P00-PWA-AUTH-WP-021C05B01 — characterize existing email reset key lifecycle

- Status: DONE; Size: S; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021A`; the owner authorized independent Phase 00 work even when C05 is not ready.
- On disposable real WordPress with synthetic users only, characterize the existing Bridge forgot/reset email flow and compare the issued transient key, key selected for the WP mail, WP core validation and the legacy reset endpoint. Capture no raw keys, credentials, email contents or personal data in output/evidence. Fake mail locally without delivery.
- Do not change any endpoint, real account, production behavior, existing site or mobile contract. Record demonstrated behavior and limits in `docs/evidence/P00-PWA-AUTH-WP-021C05B01/`. This characterization must not be presented as a fix, browser reset implementation or a passed security gate.

## Completion — 2026-10-18

- Eight characterization checks passed on each of three isolated WordPress/PHP combinations. The legacy Bridge handler rejected the key chosen for the WP email and accepted the first transient key despite core invalidating it. PHP 7.4 lint and the disposable guard passed. [Redacted evidence and limits](../evidence/P00-PWA-AUTH-WP-021C05B01/verification.md).
- This is a documented open defect, not an endpoint change or fix. No real mail, retained site or account was touched; C05 stays TODO.
