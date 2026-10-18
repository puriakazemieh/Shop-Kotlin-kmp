# P00-PWA-AUTH-WP-021C04C01 — characterize legacy OTP concurrency

- Status: DONE; Size: S; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021A`. The owner authorized independent Phase 00 work while the parent is not ready.
- In isolated synthetic real WordPress, exercise the existing mobile OTP issuer and login verifier with two independent processes and a controlled barrier after both read the same transient. Check the number of successful responses and ordinary sequential reuse. Do not print codes, tokens or personal information; intercept SMS locally.
- Do not change OTP/mobile behavior, activate web routes, modify preserved sites or claim that a fixture proves a safe verifier. Record real versions, storage/cache limitations, results and reproduction in `docs/evidence/P00-PWA-AUTH-WP-021C04C01/`. The parent remains TODO until real secure integration and its dependencies are complete.

## Completion — 2026-10-18

The characterization reproduced two successful mobile OTP logins from one code under a controlled shared-read race on three real WordPress/PHP combinations; later sequential reuse was rejected. This DONE is **only for reproducing and documenting the finding**, not a fix. [Evidence and reproduction](../evidence/P00-PWA-AUTH-WP-021C04C01/otp-concurrency.md). No live behavior changed; the parent remains open.
