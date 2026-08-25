# Evidence for P01-SECURITY-CODE-019

## Actions
- Scanned codebase for hardcoded/demo JWT secrets, ZarinPal merchant IDs, and SMS/OTP credentials.
- Verified that JWT falls back safely to WP AUTH_KEY or throws an exception if missing.
- Verified that ZarinPal reads merchant ID securely via WP get_option.
- Cleaned up dummy API URLs in PlatformConfig.android.kt, PlatformConfig.js.kt, and Brand.kt.

## Test Result
- Compile JVM task executed successfully: exit code 0.
- Codebase is production fail-closed without hardcoded secrets.
