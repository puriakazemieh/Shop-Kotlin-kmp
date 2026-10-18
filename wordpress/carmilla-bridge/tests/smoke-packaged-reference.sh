#!/bin/sh
# Fresh, disposable WordPress container; inputs are mounted read-only.
set -eu
test "${CARMILLA_REFERENCE_DISPOSABLE:-}" = 'yes'
case "${CARMILLA_REFERENCE_PREFIX:-}" in qa_[0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f]_) ;; *) exit 2 ;; esac
test -f /artifacts/carmilla-bridge.zip
test -f /artifacts/carmilla-theme.zip
test -f /woocommerce/woocommerce.php
test -f /wp-cli.phar
wp() { php /wp-cli.phar --allow-root --path=/usr/src/wordpress "$@"; }
cp -R /woocommerce /usr/src/wordpress/wp-content/plugins/woocommerce
wp config create --dbname=carmilla_compat_qa --dbuser=synthetic_qa --dbpass=synthetic_qa_password --dbhost=127.0.0.1:3306 --dbprefix="$CARMILLA_REFERENCE_PREFIX" --skip-check
wp config set WP_HTTP_BLOCK_EXTERNAL true --raw
wp core install --url=http://qa.example.test --title='Synthetic QA' --admin_user=qa_admin --admin_password=synthetic-only-password --admin_email=synthetic@example.test --skip-email
wp plugin activate woocommerce
wp plugin install /artifacts/carmilla-bridge.zip --activate
wp theme install /artifacts/carmilla-theme.zip --activate
wp eval-file /source/carmilla-bridge/tests/smoke-woo-reference.php
echo 'PASS packaged install smoke'
