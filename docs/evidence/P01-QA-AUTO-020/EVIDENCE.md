# Evidence for P01-QA-AUTO-020

## Actions
- Added smoke-security.php to run automated hermetic checks for security changes in 003-019.
- Verified fail-closed defaults for cb_course and cb_therapist CPTs.
- Executed Kotlin unit tests with ./gradlew.bat test.

## Test Result
- docker run --rm -v D:\Android\AndroidStudioProjects\kmp-shop/wordpress/carmilla-bridge:/app -w /app php:8.1-cli php tests/smoke-security.php returned exit code 0.
- Gradle test task passed successfully.
