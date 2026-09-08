# Operations Summary: P10-PROGRAM-OPS-012

- Task ID: P10-PROGRAM-OPS-012
- Date: 2026-09-06
- Title: Controlled Limited Release of Verified Product & SKU Combinations Only

## Limited Release Governance Verification

### 1. Release Candidate Manifest Audit
- Release Candidate Tag: `0.9.0-rc.1`
- **Allowed SKUs:** `SKU-THEME-BASE`, `SKU-THEME-SHOP`, `SKU-PLUGIN-BASE`, `SKU-PLUGIN-SHOP`, `SKU-THEME-BUILDER` (Android/PWA targets only).
- **Prohibited / Guarded Features:** Features from unpassed future Gates (`academy`, `clinic`, `psych_tests`, iOS/Desktop builds) are marked as **experimental** and excluded from production release candidate manifests.

### 2. Fake Builder Guard
- P04 mock builder runner is explicitly blocked from production build dispatch. Only live verified builder runners (`Android / PWA`) are exposed for commercial purchase.

## Status
- Final Status: DONE
