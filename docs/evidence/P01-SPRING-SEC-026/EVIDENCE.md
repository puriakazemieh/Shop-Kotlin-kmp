# Evidence for P01-SPRING-SEC-026

## Baseline
- Date: 2026-08-25
- Target: `ShopServer` (Spring Boot Backend)

## Fix Description
Fixed a race condition / idempotency issue in `ClinicService.cancel` which allowed cancelling an already-cancelled appointment. This caused users to receive infinite duplicate `Session Credit` refunds.

Changes made:
1. Added `@Lock(LockModeType.PESSIMISTIC_WRITE)` to `findByIdAndUserIdForUpdate` in `AppointmentRepository`.
2. Updated `ClinicService.cancel` to fetch the appointment with the database row lock.
3. Added a guard condition `if (appointment.status == AppointmentStatus.CANCELLED) return` to gracefully handle redundant cancel requests.

## Verification
- Run `gradlew.bat compileKotlin` -> Exit code 0 (BUILD SUCCESSFUL in 13s)
- Idempotency confirmed via static analysis of the new database row-locking flow.
