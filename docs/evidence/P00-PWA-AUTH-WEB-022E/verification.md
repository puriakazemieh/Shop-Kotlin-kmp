# Synthetic Spring refresh/logout ordering characterization

Date: 2026-10-19. Separate ShopServer repository test commit `bdef7af`.

`RefreshLogoutOrderingCharacterizationTest` creates a synthetic user and a mocked in-memory repository. It issues a token, rotates it, issues the replacement, then revokes the old token. The old token is rejected, but the replacement is still accepted. This reproduces the unsafe sequential behavior; it does not assert desired security behavior.

`gradlew.bat test --tests '*RefreshLogoutOrderingCharacterizationTest'` on ShopServer: BUILD SUCCESSFUL; 1 test, 0 failures. Offline execution first failed because build dependencies were not cached; online execution downloaded missing build dependencies. No real account or database was used, and no credential values are recorded here.

Limitations: direct service invocation bypasses Spring transaction interception; mock storage is not a database, and no HTTP, browser, cookie, concurrency or actual deployment was tested. It is not a fix. The system has no demonstrated way to revoke a replacement by stale predecessor without a safe family/session model. A fix must preserve other-device semantics and be tested against actual transactional race conditions before lifecycle acceptance.
