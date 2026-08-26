# P02-QA-MANUAL-017 completion evidence

## Scope and authority

- Manual smoke scope: `auth/home/product/cart/payment-return` on a synthetic fixture.
- Manual result: PASS, confirmed by the user on 2026-08-26.
- Build fingerprint: baseline commit `cefd1e94`; pre-existing user-owned untracked files were preserved.

## Automated baseline

| Command | CWD | Exit | Result |
|---|---|---:|---|
| `./gradlew.bat :composeApp:compileKotlinJvm` | `D:\Android\AndroidStudioProjects\kmp-shop` | 1 | Gradle could not establish a loopback connection. |
| `./gradlew.bat --no-daemon :composeApp:compileKotlinJvm` | `D:\Android\AndroidStudioProjects\kmp-shop` | 1 | Same loopback-connection environment failure. |

No source code was changed by this manual-QA task.

## Required release revalidation

Before release, resolve the Gradle loopback error and rerun the JVM compile. Then repeat the manual smoke test on a release candidate with synthetic data and record tester, date, and build fingerprint. Verify authentication, home, product detail, cart, and payment return. A payment return must display only the backend-authoritative state and must not clear the cart after a failed, cancelled, or unverified payment.
