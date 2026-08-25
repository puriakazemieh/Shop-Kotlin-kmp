# P01-SECURITY-CODE-005C — Verification evidence

Date: 2026-08-25

## Implemented controls

- ShopServer provides a separate web-only authentication contract under `/api/auth/web/*`.
- The refresh token is returned only through the host-bound `__Host-shop_refresh` cookie with `HttpOnly`, `Secure`, `SameSite=Strict`, `Path=/`, and no `Domain` attribute.
- Kotlin/JS stores no values in browser persistent storage; its `Settings` implementation is in-memory only.
- The Ktor JS engine uses browser fetch credentials `include`; CORS only accepts exact origins while credentials are enabled.

## Automated commands

| Working directory | Command | Result |
|---|---|---|
| `D:\Android\AndroidStudioProjects\ShopServer\Shop` | `.\gradlew.bat test --tests "com.kazemieh.shop.identity.api.WebSessionCookieFactoryTest" --console=plain` | PASS, exit 0 |
| `D:\Android\AndroidStudioProjects\ShopServer\Shop` | `docker compose up -d --wait db` سپس `.\gradlew.bat test --console=plain` | PASS, PostgreSQL 16 healthy؛۵ تست، exit 0 |
| `D:\Android\AndroidStudioProjects\kmp-shop` | `.\gradlew.bat :core:network:compileKotlinJs :core:data:compileKotlinJs :core:network:compileKotlinJvm :core:data:compileKotlinJvm --console=plain` | PASS, exit 0 |
| `D:\Android\AndroidStudioProjects\kmp-shop` | `.\gradlew.bat :core:network:compileAndroidMain :core:data:compileAndroidMain --console=plain` | PASS, exit 0 |
