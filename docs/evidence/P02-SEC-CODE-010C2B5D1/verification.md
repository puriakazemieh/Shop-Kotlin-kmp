# Root alias authentication baseline — 2026-11-03

Environment: disposable `test-env-wordpress-test-1` Apache/WordPress with Carmilla Bridge; synthetic customer, WordPress cookie/session, valid `wp_rest` nonce, signed access JWT, and synthetic user meta. Command: `docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/probe-root-alias-auth-http-real-wp.php`.

The probe sent 64 actual HTTP requests: 8 read/write route cases across profile, addresses, favorites, recently viewed; 4 authentication modes (anonymous, cookie without REST nonce, cookie with valid nonce, bearer); root and canonical paths. `http-matrix.txt` records status/error code and whether user state changed. Transport failures: 0; synthetic user/session/meta cleanup: PASS; PHP lint: PASS.

Finding: all 8 cookie/no-nonce root requests bypassed native REST cookie authentication. Four writes changed user meta through root aliases while canonical routes returned 401 and changed nothing. Anonymous and bearer paths matched canonical outcomes; valid cookie+nonce paths also matched. D2 must route requests through the native authentication filter before completion of the parent security task. No production data or external service used. This card characterizes the flaw and does not claim the alias is secure.
