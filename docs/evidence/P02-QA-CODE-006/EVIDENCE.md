# Evidence for P02-QA-CODE-006

## Actions
- Configured commonTest dependencies in carmilla.kmp.library.gradle.kts using kotlin(\"test\").
- Added EmailValidatorTest.kt inside core/domain/src/commonTest.
- Added KtorClientTest.kt inside core/network/src/commonTest.
- Ran .\gradlew.bat :core:domain:allTests :core:network:allTests successfully on CI.
