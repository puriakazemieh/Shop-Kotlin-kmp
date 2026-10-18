#!/bin/sh
# Disposable CLI container only; the supplied WordPress copy is read-only.
set -eu
test "${CARMILLA_REFERENCE_DISPOSABLE:-}" = 'yes'
case "${CARMILLA_REFERENCE_PREFIX:-}" in qa_[0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f]_) ;; *) exit 2 ;; esac
test -f /wordpress-source/wp-settings.php
test ! -e /usr/src/wordpress
mkdir -p /usr/src/wordpress
cp -R /wordpress-source/. /usr/src/wordpress/
docker-php-ext-install -j2 mysqli >/dev/null
exec sh /source/carmilla-bridge/tests/smoke-woo-reference.sh
