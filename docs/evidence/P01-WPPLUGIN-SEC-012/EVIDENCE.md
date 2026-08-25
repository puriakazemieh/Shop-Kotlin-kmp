# Evidence for P01-WPPLUGIN-SEC-012

## Implementation Details
- Modified class-cb-plugin.php CORS configuration to replace permissive * origin.
- Configured an allowlist of origins combining get_site_url(), localhost development ports, and an optional CB_ALLOWED_ORIGINS environment constant.
- Configured Access-Control-Allow-Origin: null for unapproved origins.
- Ensures Access-Control-Allow-Credentials: true is only sent to approved origins to protect against CSRF and token stealing.

## Automated Tests
- Command: docker run --rm -v D:\Android\AndroidStudioProjects\kmp-shop:/app -w /app php:8.1-cli php -l wordpress/carmilla-bridge/includes/class-cb-plugin.php`n- Result: No syntax errors detected

## Reviewer
AI Agent

## Date
2026-08-25
