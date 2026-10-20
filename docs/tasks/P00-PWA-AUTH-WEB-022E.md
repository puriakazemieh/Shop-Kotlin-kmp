# P00-PWA-AUTH-WEB-022E — characterize Spring refresh/logout ordering

- Status: DONE; Size: S; Owner: AI; Risk: HIGH
- Parent: `P00-PWA-AUTH-WEB-022`; independently testable without enabling WordPress routes.
- With synthetic data and a mocked repository, characterize whether logout with an already-rotated refresh token revokes the replacement. Document the scope and limits of this test. Do not touch real users, the database, cookies or application behavior.
- Record redacted evidence under `docs/evidence/P00-PWA-AUTH-WEB-022E/`. A reproduced defect is not a fix or permission to claim end-to-end session safety.

## Result — 2026-10-19

The defect was reproduced first with a mocked repository, and then using transactional Spring services with an isolated PostgreSQL test container. ShopServer commit `bdab55f` then added a refresh-token family migration and revokes the full family when a predecessor is logged out; both focused and transactional PostgreSQL tests now pass. These are still not concurrent HTTP, cookie or browser tests. The WordPress parent dependency and owner QA keep the parent blocked. See [evidence](../evidence/P00-PWA-AUTH-WEB-022E/verification.md).
