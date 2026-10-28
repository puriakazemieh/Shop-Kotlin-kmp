<?php
/** Local-only browser-session configuration; mounted by the QA profile only. */
if ( ! defined( 'ABSPATH' ) || defined( 'CB_WEB_SESSION_ENABLED' ) || ! defined( 'AUTH_KEY' ) || ! is_string( AUTH_KEY ) || AUTH_KEY === '' ) {
	return;
}

define( 'CB_WEB_SESSION_ENABLED', true );
define( 'CB_WEB_SESSION_ENVIRONMENT', 'local' );
define( 'CB_WEB_SESSION_LOCAL_QA', true );
define( 'CB_WEB_SESSION_TRUSTED_INGRESS', true );
define( 'CB_WEB_SESSION_SITE_URL', 'http://localhost:8092' );
define( 'CB_WEB_SESSION_ALLOWED_ORIGINS', array( 'http://localhost:8081' ) );
define( 'CB_WEB_SESSION_LOCAL_QA_CLIENT_IPS', array( '172.20.0.250' ) );
define( 'CB_WEB_SESSION_SECRET_HEX', hash( 'sha256', AUTH_KEY . "\0carmilla-local-web-session-v1" ) );
