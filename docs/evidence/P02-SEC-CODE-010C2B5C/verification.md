# Manifest notice input shape — 2026-11-03

The manifest admin page accepts only a string `cb_manifest_error`, unslashes and sanitizes it, then escapes the final notice. Array and object values no longer become a misleading error message or trigger PHP warnings/fatal errors. The fixed success notice remains visible when `cb_manifest_updated` is present. No settings save path, capability gate, or nonce gate changed.

GitNexus pre-edit impact for `CB_Manifest_Controller` reported MEDIUM, 66 upstream symbols, with apparent unrelated Kotlin edges; source inspection confirmed the admin render callback and public REST controller. The change is confined to notice rendering.

Before the fix, the installed WordPress render probe showed scalar PASS, array FAIL with a PHP warning, then a fatal error for an object value. After the fix, command `docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/smoke-manifest-notice-real-wp.php` passed all four cases: scalar escaped, array ignored with success notice, object ignored, and fixed success notice. Each case had zero warnings and unchanged `cb_manifest_features` option (`verification-output.txt`). Synthetic admin accounts are removed by the probe. PHP lint of controller and probe passed. No owner manual QA is required for this task.
