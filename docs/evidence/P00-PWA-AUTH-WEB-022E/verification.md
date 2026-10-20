# Synthetic Spring refresh/logout ordering characterization

Date: 2026-10-19. Separate ShopServer repository test commit `bdef7af`.

`RefreshLogoutOrderingCharacterizationTest` creates a synthetic user and a mocked in-memory repository. It issues a token, rotates it, issues the replacement, then revokes the old token. The old token is rejected, but the replacement is still accepted. This reproduces the unsafe sequential behavior; it does not assert desired security behavior.

`gradlew.bat test --tests '*RefreshLogoutOrderingCharacterizationTest'` on ShopServer: BUILD SUCCESSFUL; 1 test, 0 failures. Offline execution first failed because build dependencies were not cached; online execution downloaded missing build dependencies. No real account or database was used, and no credential values are recorded here.

Limitations: direct service invocation bypasses Spring transaction interception; mock storage is not a database, and no HTTP, browser, cookie, concurrency or actual deployment was tested. It is not a fix. The system has no demonstrated way to revoke a replacement by stale predecessor without a safe family/session model. A fix must preserve other-device semantics and be tested against actual transactional race conditions before lifecycle acceptance.

## Follow-up with real test database

ShopServer commit `fc1fd6e` adds the same sequential characterization using the actual Spring `AuthService` and transactional services on an isolated Testcontainers PostgreSQL 15 database. The old token was rejected and the replacement was still usable after logout with the old token. The targeted `gradlew.bat test --tests '*ShopApplicationTests.stale logout does not revoke rotated session in transactional database' --offline` completed successfully: one characterization test passed. No retained database or real accounts were used. The full preexisting server test suite also returned `BUILD SUCCESSFUL`; during test teardown an existing scheduled job logged a closed-database error after the disposable database stopped, so this is not evidence of a clean scheduler shutdown.

## Resolved server defect — 2026-10-20

ShopServer commit `bdab55f` adds a non-null refresh-token family identifier through `V2__refresh_token_family.sql`, backfills legacy rows, preserves the family during rotation, and revokes every active member of that family when any predecessor is revoked. The Spring transactional PostgreSQL test now verifies that logout with the old rotated token also invalidates the replacement. The focused ordering test and the real Testcontainers test both completed with `BUILD SUCCESSFUL`.

The migration cannot infer a historical family relationship that was never stored; each pre-migration legacy row is therefore assigned its own safe family. HTTP/browser/cookie integration with the WordPress path still requires its own evidence and owner QA.
