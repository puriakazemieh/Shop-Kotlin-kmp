<?php
/** Disposable installed WordPress render probe; no manifest setting is saved. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
require_once ABSPATH . 'wp-admin/includes/template.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-manifest-controller.php';

foreach ( get_users( array( 'search' => 'qa_manifest_notice_*', 'search_columns' => array( 'user_login' ) ) ) as $stale_user ) {
	wp_delete_user( $stale_user->ID );
}
$user = wp_create_user( 'qa_manifest_notice_' . bin2hex( random_bytes( 4 ) ), wp_generate_password( 24 ), 'qa-manifest-notice@example.test' );
if ( ! is_int( $user ) ) { exit( 2 ); }
( new WP_User( $user ) )->set_role( 'administrator' );
wp_set_current_user( $user );
register_shutdown_function( static function () use ( $user ) { wp_delete_user( $user ); } );
$before = get_option( 'cb_manifest_features' );
$cases = array(
	array( 'scalar', array( 'cb_manifest_error' => 'bad&feature' ), true, false ),
	array( 'array', array( 'cb_manifest_error' => array( 'bad' ), 'cb_manifest_updated' => '1' ), false, true ),
	array( 'object', array( 'cb_manifest_error' => (object) array( 'bad' => 'value' ) ), false, false ),
	array( 'success', array( 'cb_manifest_updated' => '1' ), false, true ),
);
$failed = 0;
foreach ( $cases as $case ) {
	list( $name, $_GET, $expect_error, $expect_success ) = $case;
	$warnings = array();
	set_error_handler( static function ( $severity, $message ) use ( &$warnings ) { $warnings[] = $message; return true; } );
	ob_start();
	( new CB_Manifest_Controller() )->render_admin_page();
	$html = ob_get_clean();
	restore_error_handler();
	$has_error = false !== strpos( $html, 'Saved nothing.' );
	$has_success = false !== strpos( $html, 'Manifest updated.' );
	$escaped = 'scalar' !== $name || ( false !== strpos( $html, 'bad&amp;feature' ) && false === strpos( $html, 'bad&feature' ) );
	$ok = $has_error === $expect_error && $has_success === $expect_success && $escaped && empty( $warnings ) && get_option( 'cb_manifest_features' ) === $before;
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . $name . ' error=' . (int) $has_error . ' success=' . (int) $has_success . ' escaped=' . (int) $escaped . ' warnings=' . count( $warnings ) . ' unchanged=' . (int) ( get_option( 'cb_manifest_features' ) === $before ) . "\n";
	$failed += $ok ? 0 : 1;
}
exit( $failed ? 1 : 0 );
