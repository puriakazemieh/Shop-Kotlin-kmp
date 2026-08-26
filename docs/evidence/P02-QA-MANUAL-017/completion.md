# P02-QA-MANUAL-017 release handoff

## Scope and authority

- Manual smoke scope: `auth/home/product/cart/payment-return` on a synthetic fixture.
- Manual result: pending; execute only against the release candidate.
- Build fingerprint: record the release-candidate commit and version at execution time.

## Automated baseline

| Command | CWD | Exit | Result |
|---|---|---:|---|
| `./gradlew.bat :composeApp:compileKotlinJvm` | `D:\Android\AndroidStudioProjects\kmp-shop` | 1 | Gradle could not establish a loopback connection. |
| `./gradlew.bat --no-daemon :composeApp:compileKotlinJvm` | `D:\Android\AndroidStudioProjects\kmp-shop` | 1 | Same loopback-connection environment failure. |

No source code was changed by this manual-QA task. Pre-existing user-owned untracked files were preserved.

## Required manual QA at release

Before release, resolve the Gradle loopback error and rerun the JVM compile. Then run the manual smoke test on a release candidate with synthetic data and record tester, date, and build fingerprint. Verify authentication, home, product detail, cart, and payment return. A payment return must display only the backend-authoritative state and must not clear the cart after a failed, cancelled, or unverified payment.
