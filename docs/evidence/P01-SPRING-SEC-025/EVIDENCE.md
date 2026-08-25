# Evidence for P01-SPRING-SEC-025

## Actions
- Moved /uploads/** from permitAll to authenticated in SecurityConfig.
- Video URLs are already gated by enrollment check in CourseService (line 52).
- Enrollment requires payment flow (noted as TODO in code comment line 96-97).

## Verification
- gradlew.bat compileKotlin exit code 0.

## Remaining
- Signed URL implementation deferred to future task (requires S3/MinIO integration).
- Enrollment paywall enforcement deferred — comment in CourseService.enroll() marks this as a TODO.
