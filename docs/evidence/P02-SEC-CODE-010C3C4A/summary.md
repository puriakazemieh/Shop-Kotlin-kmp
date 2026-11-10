# P02-SEC-CODE-010C3C4A — integration test-message scan drift

Date: 2026-11-10. Only the test-message exception path and its focused smoke were changed. The provider exception is ignored, no message/recipient/provider value is logged, and the fixed translated error is escaped before `wp_die`. Success, capability and POST nonce checks remain on the same path.

## Impact and read-only GET review

GitNexus `impact handle_test_message --direction upstream` returned `UNKNOWN` because it could not resolve its callback receiver. Text search confirmed registration through `admin_post_carmilla_test_message` in `init()` and the focused smoke's direct call; the installed WordPress HTTP request exercised the real callback. No zero-caller safety inference was made.

The three `WordPress.Security.NonceVerification.Recommended` warnings at `class-cb-integrations-controller.php:78` are reads of `$_GET['tab']` in `render_admin_page`. The method first requires `can_manage_integrations()`, sanitizes the value and restricts it to `sms`, `email`, `templates`, `health`, or `test`; this value selects an admin display branch and performs no mutation. The POST handler separately requires `check_admin_referer`. These three exact warnings remain visible and are assigned to C3C4D for individually reviewed baseline treatment; no global PHPCS or Plugin Check suppression was added.

## Verification

- `php -l` on the controller and focused smoke in `php:8.1-cli`: both passed. `smoke-integration-log-redaction.php success|failure`: both passed with empty log on both paths.
- Built the delivery-shaped ZIP with `wordpress/build-bridge-zip.sh` in disposable Alpine; ZIP SHA-256 `019f2ca260d08882fa3673ddcba7e5ad223f728369c31b6d6f01515666c8a0bd`.
- Disposable WordPress 6.4.3/PHP 8.1/MariaDB 10.6, installed and activated that ZIP and Plugin Check 2.1.0. The synthetic HTTP failure POST to `wp-admin/admin-post.php` returned a 4xx/5xx with only `Failed to send test message.`; recipient, body and provider markers were absent. Apache/PHP log contained none of these markers or the former generic error log. The existing installed-WordPress HTTP smoke passed admin success redirect, invalid type denial, nonce denial and synthetic-user cleanup (4/4).
- Pinned Plugin Check on the installed ZIP: 314 findings before, 312 after. The `error_log_error_log` warning at `51:13` and `OutputNotEscaped` error at `52:20` disappeared. The complete after-report is [plugin-check-after.txt.gz](plugin-check-after.txt.gz), uncompressed SHA-256 `5cc2c515bbb5ce3f0e3f3be614f18a64ab0e78ecfeaafb2dcfc3f4c70c61e98a`.
- The exact baseline remains correctly red on the three reviewed GET warnings, the separate mixed-line-ending finding and the wallet direct-query finding. These belong to C3C4B–D; this card does not claim a green Plugin Check or wallet gate.

The disposable environment uses only synthetic accounts and an intercepted mail provider. No owner test is needed for this bounded change.
