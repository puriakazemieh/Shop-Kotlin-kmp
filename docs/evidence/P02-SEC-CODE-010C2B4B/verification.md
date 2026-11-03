# Meta-box scalar input validation — 2026-11-03

The real installed-WordPress `save_post` callback accepted a posted array for `cb_link_url` and called `esc_url_raw` with it. Baseline probe exited 255 with `TypeError: ltrim(): ... array given` (`baseline.txt`). A scalar shape guard now skips malformed array/object values before `wp_unslash`; type-specific URL, number, textarea and text sanitization remains unchanged.

Verification: `docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/smoke-meta-box-scalar-shape-real-wp.php` → exit 0, 7 PASS, 0 FAIL (`verification-output.txt`). It checks arrays for text/number/URL are ignored, invalid nonce and missing capability deny writes, valid scalars are sanitized, and synthetic post/user are deleted. PHP 8.1 lint passed for source and test. No real customer data was used.

Pre-edit GitNexus file impact was `risk: UNKNOWN`, zero resolved callers/processes; source confirms the `save_post` hook registration at `meta-boxes.php:116`. The graph result was not treated as evidence of no callers.

Targeted WPCS 3.4.1/PHPCS 3.13.6 rerun (`phpcs-targeted.json`) exited 1 and still reports `WordPress.Security.ValidatedSanitizedInput.InputNotSanitized` at the `wp_unslash` line. The scanner does not infer the preceding scalar guard plus subsequent type-specific sanitization. No broad suppression was added. The behavioral failure is fixed and verified; the static finding remains for the CI disposition task to handle explicitly.
