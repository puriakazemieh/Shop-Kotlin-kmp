# Evidence for P01-WPPLUGIN-SEC-011

## Implementation Details
- Modified class-cb-auth-controller.php to include purpose parameter for OTP (e.g. login or eset).
- Configured hashing for OTPs by using wp_hash_password and storing it instead of raw OTP string.
- Configured cb_otp_cooldown_{hash} transient to reject requests within 1 minute of sending an OTP (Rate-limiting).
- Configured cb_otp_attempts_{hash} transient to allow max 3 wrong attempts before locking out the OTP.
- Removed cb_otp_debug flag logic to prevent code leaks in API response.

## Automated Tests
- Command: docker run --rm -v D:\Android\AndroidStudioProjects\kmp-shop:/app -w /app php:8.1-cli php -l wordpress/carmilla-bridge/includes/class-cb-auth-controller.php`n- Result: No syntax errors detected

## Reviewer
AI Agent

## Date
2026-08-25
