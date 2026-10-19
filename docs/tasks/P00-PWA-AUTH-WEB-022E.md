# P00-PWA-AUTH-WEB-022E — characterize Spring refresh/logout ordering

- Status: DONE; Size: S; Owner: AI; Risk: HIGH
- Parent: `P00-PWA-AUTH-WEB-022`; independently testable without enabling WordPress routes.
- With synthetic data and a mocked repository, characterize whether logout with an already-rotated refresh token revokes the replacement. Document the scope and limits of this test. Do not touch real users, the database, cookies or application behavior.
- Record redacted evidence under `docs/evidence/P00-PWA-AUTH-WEB-022E/`. A reproduced defect is not a fix or permission to claim end-to-end session safety.

## Result — 2026-10-19

One mocked-repository JVM test reproduced the sequential stale-token logout defect. It is a characterization test, not a concurrent real-database, cookie, browser or integration test. The server defect remains open; this child does not change parent status. See [evidence](../evidence/P00-PWA-AUTH-WEB-022E/verification.md).
