# P02-SEC-CODE-010C3B3C1 — integration test-message redaction

Date: 2026-11-10. `CB_Integrations_Controller::handle_test_message` no longer logs a partially redacted recipient on success. On provider failure, it logs only `Carmilla test message failed.` and returns a fixed translated error instead of the exception message. Admin capability, nonce, validation, test delivery and success redirect remain in the same call path.

Impact: GitNexus upstream impact for `handle_test_message` returned `UNKNOWN`/zero resolved callers on a stale MCP view; text search confirmed `init()` registers it with `admin_post_carmilla_test_message`, and the installed-plugin HTTP fixture exercised that route. No zero-caller safety inference was used.

Checks:

1. `php -l` for the changed controller and focused smoke file in cached WordPress 6.4/PHP 8.1 container: both passed.
2. `php wordpress/carmilla-bridge/tests/smoke-integration-log-redaction.php success|failure` in that container: `PASS success` and `PASS failure`. Synthetic recipient `09123456789`, body marker and provider error marker were absent from the modeled response, redirect and error log. No mail/SMS delivery occurred.
3. Fresh disposable WordPress 6.4.3/PHP 8.1/MariaDB 10.6 on isolated Docker network, synthetic administrator and installed Bridge ZIP with only this controller overlaid. A temporary must-use filter intercepted mail. The existing `smoke-integrations-form-http-real-wp.php` produced four PASS results: admin email redirect, invalid type denial, nonce denial and synthetic-admin cleanup.
4. A separate synthetic HTTP POST to `wp-admin/admin-post.php` forced mail failure. It returned a non-redirect 4xx/5xx response with `Failed to send test message.` and no recipient, body or provider marker. The Apache/PHP log contained only `Carmilla test message failed.` for that request and no marker. The success HTTP path retained 302 and delivery interception.

The disposable containers and network were removed. No external account, production message, owner judgment or manual QA is required for this bounded privacy fix. This does not approve unrelated messaging or Plugin Check gates.
