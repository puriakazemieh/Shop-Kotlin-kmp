# P00-PWA-AUTH-WP-021C04C — isolated guarded OTP login

- Status: BLOCKED; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B`.
- Review and implement/test safe one-time OTP verification, unique mobile-to-user resolution and guarded login with WP authentication filters. Include invalid, expired, reused and concurrently submitted OTP, duplicate mobile identities, first-use account creation and failure/redaction. No acceptance of an unverified or ambiguously owned mobile number.
- The existing private transient verifier is a non-atomic read/check/delete. A mock verifier is not proof of actual integration; account for object caches and concurrent legacy mobile requests or fail closed for unsupported configurations. Preserve mobile contract; any security-sensitive shared change requires focused regression and real WP/database evidence. If larger than M, split before implementation.
- No public routes, live network change, migration or modification of preserved WordPress sites. Evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04C/`. Human network QA remains `021C07`.

Independent characterization `021C04C01` reproduced two successful legacy mobile logins using one OTP under a controlled race on three real synthetic WordPress versions. [Evidence](../evidence/P00-PWA-AUTH-WP-021C04C01/otp-concurrency.md). It does not fix the verifier, prove a cache configuration safe, or complete this card.

`021C04C02` adds an [atomic consumer](../evidence/P00-PWA-AUTH-WP-021C04C02/verification.md). It is now connected to the existing OTP routes; [route-level synthetic evidence](../evidence/P00-PWA-AUTH-WP-021C04C/verification.md) covers first use, reuse, attempt locking, cache rejection and the concurrent consumer. Persistent object caches fail closed rather than falling back to unsafe behavior. This parent remains BLOCKED because `021C04B` is blocked and human route QA is tracked in `021C04C03`.
