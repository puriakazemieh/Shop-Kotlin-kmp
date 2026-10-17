# P00-PWA-AUTH-WP-021C04C — isolated guarded OTP login

- Status: TODO; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B`.
- Review and implement/test safe one-time OTP verification, unique mobile-to-user resolution and guarded login with WP authentication filters. Include invalid, expired, reused and concurrently submitted OTP, duplicate mobile identities, first-use account creation and failure/redaction. No acceptance of an unverified or ambiguously owned mobile number.
- The existing private transient verifier is a non-atomic read/check/delete. A mock verifier is not proof of actual integration; account for object caches and concurrent legacy mobile requests or fail closed for unsupported configurations. Preserve mobile contract; any security-sensitive shared change requires focused regression and real WP/database evidence. If larger than M, split before implementation.
- No public routes, live network change, migration or modification of preserved WordPress sites. Evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04C/`. Human network QA remains `021C07`.
