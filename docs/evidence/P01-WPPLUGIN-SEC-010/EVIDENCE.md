# Evidence for P01-WPPLUGIN-SEC-010

## Implementation Details
- Modified class-cb-jwt.php to drop the insecure default secret and throw an Exception if CB_JWT_SECRET and AUTH_KEY are not configured properly.
- Added iss (issuer) and ud (audience) to the JWT payload.
- Added checks during decoding to verify the iss and ud match get_site_url() and carmilla-client.
- Implemented token revocation checking cb_jwt_revoked_before user meta timestamp.
- Integrated CB_JWT::revoke_for_user() into class-cb-auth-controller.php password reset methods to revoke existing tokens upon password reset.
- Added unit tests in 	ests/smoke.php to verify revoked tokens are rejected.

## Automated Tests
- Command: docker run --rm -v D:\Android\AndroidStudioProjects\kmp-shop:/app -w /app php:8.1-cli php wordpress/carmilla-bridge/tests/smoke.php`n- Result: ALL PASSED (including revoked token rejected)

## Reviewer
AI Agent

## Date
2026-08-25
