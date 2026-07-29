# Carmilla Project: Rules to Keep (RTK)

This document outlines the technical standards, forbidden patterns, and environmental rules for the Carmilla project.

## Technical Standards
- **Android**: `targetSdk = 36`, `minSdk = 24`, AGP 8.11+ (preparing for 9.0).
- **KMP**: Kotlin 2.x, Compose Multiplatform.
- **WordPress**: Minimum PHP 7.4, WooCommerce compatible.
- **Backend**: Only two profiles: `WORDPRESS` and `SPRING`.

## Forbidden Patterns (Stop-Ship)
1. **Business Logic in Themes**: WordPress themes must be presentation-only. No CPT registration, REST controller logic, or payment handling inside the theme.
2. **Direct SQL for Orders**: Always use WooCommerce CRUD or Store API.
3. **Non-Atomic Financials**: Wallet withdrawals and Booking slots must use locks/transactions. No read-modify-write on user-meta arrays.
4. **Hardcoded Secrets**: No hardcoded JWT secrets or merchant keys.
5. **Permissive CORS**: No `Access-Control-Allow-Origin: *` in production. Use tenant allowlist.

## Repository Hygiene
- **Commit Messages**: Reference Task ID (e.g., `feat(auth): fix P01-011 OTP hardening`).
- **Task Sizes**:
  - **XS/S**: Simple doc or small module fix.
  - **M**: Vertical slice between two boundaries.
  - **L/XL**: MUST be broken down before AI execution.

## Test Environment (PHP/WP)
Since the host environment lacks PHP CLI, use Docker:
```bash
docker run --rm -v ${PWD}:/app -w /app php:8.1-cli php -l your-file.php
```
For WordPress environment tests, refer to `tools/test-env/docker-compose.yml`.
