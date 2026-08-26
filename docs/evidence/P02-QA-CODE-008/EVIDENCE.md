# Evidence for P02-QA-CODE-008

## Actions
- Added `testcontainers`, `junit-jupiter`, and `postgresql` testcontainer dependencies to `ShopServer/Shop/build.gradle.kts`.
- Configured `ShopApplicationTests.kt` with `@Testcontainers` and `@ServiceConnection` using a PostgreSQL 15-alpine image to ensure tests run completely independently of manual DB setup.
- Validated that `.\gradlew.bat test` runs and passes successfully on the Spring Boot Server.
- Executed `.\gradlew.bat :composeApp:compileKotlinJvm` in KMP client as per baseline requirements.

## Final Status
DONE
