# Root alias REST dispatch hardening — 2026-11-09

`CB_Plugin::maybe_root_alias` now sets the canonical `rest_route` and calls WordPress `rest_api_loaded()`. WordPress's normal REST server handles cookie nonce authentication, bearer authentication, HTTP headers, method, query/body parsing, route validation, permissions, and JSON response. The former direct `rest_do_request()` bypassed the REST authentication filter. The alias telemetry logs only one of four fixed route family names, without path IDs or query values.

GitNexus pre-edit impact for `maybe_root_alias` was `UNKNOWN`/ambiguous. Source inspection identified its `parse_request` registration and the four account/extras route families; no zero-caller claim was used. WordPress's installed `rest_api_loaded()` and `WP_REST_Server::serve_request()` implementation was inspected before changing dispatch.

Baseline D1 (`../P02-SEC-CODE-010C2B5D1/http-matrix.txt`) found 8 cookie/no-nonce root bypasses, including 4 writes. After the fix, run:

`docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/probe-root-alias-auth-http-real-wp.php`

All 32 root/canonical HTTP pairs passed, zero transport failures and zero auth/parity findings (`auth-matrix.txt`). Anonymous and cookie without nonce returned matching 401 for read and write; valid nonce and bearer requests matched successful canonical status/body/state changes. Synthetic user/session/meta cleanup passed.

`docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/probe-root-alias-malformed-http-real-wp.php`

Six malformed/query/header/body cases passed with root/canonical status/body parity and root-only deprecation header (`malformed-matrix.txt`): array `page`/`size`, nested query, invalid nonce, invalid bearer, and form-encoded write. No PHP warnings appeared in the container log during those probes. The alias telemetry log contained fixed family names only. PHP lint passed for source and new probe. Tenant/user isolation is separately covered by D3. No owner manual QA is required for this task.
