# Integration test-message form — 2026-11-03

The admin handler now requires string input for message type, recipient and body, applies `wp_unslash` before type-specific sanitization, rejects unsupported types, and uses `wp_safe_redirect` to the fixed admin destination. The read-only `tab` selector also unslashes and uses a known-tab allowlist. Capability and nonce gates remain in place. No production provider configuration was changed.

Pre-edit GitNexus method/file impact could not resolve the integration controller and returned `UNKNOWN`; no zero-caller safety claim was made. Source inspection confirmed `admin_post_carmilla_test_message` and admin submenu registrations in the controller's `init()` method.

Baseline installed-WordPress callback probe (`baseline.txt`): slashed email body was not normalized, unsupported type redirected as success, and an array tab did not select a valid form. After the fix, `smoke-integrations-form-real-wp.php` ran six modes in disposable installed WordPress: email body and redirect, unsupported type, array input, array tab, invalid nonce and missing capability. All six passed (`verification-output.txt`). `pre_wp_mail` intercepted the email; no SMS path or external delivery was invoked.

Actual HTTP admin-post test (`smoke-integrations-form-http-real-wp.php`) used a synthetic admin cookie/session and nonce against disposable Apache WordPress. The temporary `qa-integration-mail-mu.php` hook returned before any mail transport; its test headers proved interception and the normalized body. HTTP success redirected to the fixed admin URL; invalid type and invalid nonce did not redirect or deliver. Four checks passed, zero failed (`http-output.txt`); synthetic admin, session, temporary MU plugin and marker were removed.

PHP lint passed for source and test scripts. Targeted WPCS `ValidatedSanitizedInput` and `SafeRedirect` check exited 0 with zero errors/warnings (`phpcs-targeted.json`). The complete WPCS gate is separate. No owner manual QA is required for this task.
