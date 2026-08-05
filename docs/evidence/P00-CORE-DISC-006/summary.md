# Evidence Summary: P00-CORE-DISC-006

## Task Outcome
A snapshot of current API contracts across KMP, WordPress, and Spring Boot has been created, with specific mismatches in sort parameters and deep links identified.
**خلاصه فارسی**: وضعیت فعلی قراردادهای API در پلتفرم‌های KMP، وردپرس و اسپرینگ‌بوت ثبت شد و مغایرت‌های شناسایی‌شده در پارامترهای مرتب‌سازی و دی‌لینک‌ها گزارش گردید.

## Metadata
- **Timestamp**: 2026-08-05T12:45:00.000000000+03:30
- **Executor**: AI
- **Status**: CODE_COMPLETE (Ready for Review)

## Files Created/Modified
- `docs/inventory/api-contract-snapshot.md` (NEW)

## Execution Proof
1. Audited `AuthApiImpl.kt` and `CatalogApiImpl.kt` for client endpoints.
2. Audited `class-cb-auth-controller.php` and `class-cb-catalog-controller.php` for WordPress routes.
3. Audited `AuthController.kt` and `CatalogController.kt` in the Spring repo for parity.
4. Compared `AndroidManifest.xml` with WordPress `helpers.php` for deep link logic.

## Key Findings
- **Deep Link Mismatch**: `myapp://` vs `carmilla://`.
- **Sort Mismatch**: `price_asc` vs `price,asc`.
- **Missing Endpoints**: Logout is implemented in Spring but missing in WordPress.
