#!/bin/sh
# Disposable container only. All copied application files live in this container.
set -eu
test "${CARMILLA_REFERENCE_DISPOSABLE:-}" = 'yes'
case "${CARMILLA_REFERENCE_PREFIX:-}" in qa_[0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f]_) ;; *) exit 2 ;; esac
test -f /woocommerce/woocommerce.php
test -f /wp-cli.phar
test -f /source/packages/carmilla-core/Carmilla_Kernel.php
wp() { php /wp-cli.phar --allow-root --path=/usr/src/wordpress "$@"; }
cp -R /woocommerce /usr/src/wordpress/wp-content/plugins/woocommerce
cp -R /source/carmilla-bridge /usr/src/wordpress/wp-content/plugins/carmilla-bridge
cp -R /source/carmilla-theme /usr/src/wordpress/wp-content/themes/carmilla-theme
mkdir -p /usr/src/wordpress/wp-content/plugins/carmilla-bridge/packages /usr/src/wordpress/wp-content/themes/carmilla-theme/packages
cp -R /source/packages/carmilla-core /usr/src/wordpress/wp-content/plugins/carmilla-bridge/packages/carmilla-core
cp -R /source/packages/carmilla-core /usr/src/wordpress/wp-content/themes/carmilla-theme/packages/carmilla-core
wp config create --dbname=carmilla_compat_qa --dbuser=synthetic_qa --dbpass=synthetic_qa_password --dbhost=127.0.0.1:3306 --dbprefix="$CARMILLA_REFERENCE_PREFIX" --skip-check
wp config set WP_HTTP_BLOCK_EXTERNAL true --raw
wp core install --url=http://qa.example.test --title='Synthetic QA' --admin_user=qa_admin --admin_password=synthetic-only-password --admin_email=synthetic@example.test --skip-email
wp plugin activate woocommerce
wp plugin activate carmilla-bridge
wp theme activate carmilla-theme
wp eval-file /source/carmilla-bridge/tests/smoke-woo-reference.php
