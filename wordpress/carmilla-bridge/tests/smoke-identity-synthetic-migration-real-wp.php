<?php
/** Disposable installed WordPress only; emits no account identifiers. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { fwrite( STDERR, "Disposable flag required.\n" ); exit( 2 ); }
require '/var/www/html/wp-load.php';
$bridge = is_dir( '/work/includes' ) ? '/work' : dirname( __DIR__ );
require_once $bridge . '/includes/class-cb-identity-exact-audit.php';
require_once $bridge . '/includes/class-cb-identity-synthetic-migration.php';

global $wpdb;
$checks = 0; $failures = 0;
$check = static function ( bool $ok, string $label ) use ( &$checks, &$failures ) { ++$checks; if ( ! $ok ) { ++$failures; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n"; };
// This test intentionally creates a collision. Remove only its own previous
// synthetic accounts so a second run measures the same fixture, never an old run.
$previous_ids = $wpdb->get_col( "SELECT ID FROM {$wpdb->users} WHERE user_login LIKE 'migrate_a_%' OR user_login LIKE 'migrate_b_%' OR user_login LIKE 'migrate_c_%'" );
if ( ! is_array( $previous_ids ) || $wpdb->last_error !== '' ) { fwrite( STDERR, "Synthetic cleanup lookup failed.\n" ); exit( 2 ); }
foreach ( $previous_ids as $previous_id ) { require_once ABSPATH . 'wp-admin/includes/user.php'; if ( ! wp_delete_user( (int) $previous_id ) ) { fwrite( STDERR, "Synthetic cleanup failed.\n" ); exit( 2 ); } }
$suffix = substr( hash( 'sha256', microtime( true ) . random_int( 1, PHP_INT_MAX ) ), 0, 12 );
$a = wp_insert_user( array( 'user_login' => 'migrate_a_' . $suffix, 'user_email' => 'migrate_a_' . $suffix . '@example.test', 'user_pass' => 'synthetic-only-password' ) );
$b = wp_insert_user( array( 'user_login' => 'migrate_b_' . $suffix, 'user_email' => 'migrate_b_' . $suffix . '@example.test', 'user_pass' => 'synthetic-only-password' ) );
if ( is_wp_error( $a ) || is_wp_error( $b ) || ! $a || ! $b || ! add_user_meta( $a, 'cb_mobile', 'mobile_a_' . $suffix, true ) || ! add_user_meta( $b, 'cb_mobile', 'mobile_b_' . $suffix, true ) ) { fwrite( STDERR, "Synthetic fixture failed.\n" ); exit( 2 ); }

$migration = new CB_Identity_Synthetic_Migration( $wpdb );
$run = '11111111-1111-4111-8111-111111111111';
$plan = $migration->plan();
$check( $plan['status'] === 'READY' && $plan['target_count'] === 2, 'clean exact audit permits a bounded plan' );
$applied = $migration->apply( $run );
$check( $applied['status'] === 'APPLIED' && $applied['target_count'] === 2, 'journalled marker migration commits atomically' );
$check( get_user_meta( $a, 'cb_identity_managed', true ) === '1' && get_user_meta( $b, 'cb_identity_managed', true ) === '1' && get_user_meta( $a, 'cb_identity_migration_run', true ) === $run, 'only mobile owners receive attributable markers' );
$check( $migration->rollback( $run )['status'] === 'ROLLED_BACK' && get_user_meta( $a, 'cb_identity_managed', true ) === '' && get_user_meta( $b, 'cb_identity_migration_run', true ) === '', 'untouched run rolls back its own markers' );

$conflict = wp_insert_user( array( 'user_login' => 'migrate_c_' . $suffix, 'user_email' => 'migrate_c_' . $suffix . '@example.test', 'user_pass' => 'synthetic-only-password' ) );
if ( is_wp_error( $conflict ) || ! add_user_meta( $conflict, 'cb_mobile', 'mobile_a_' . $suffix, true ) ) { fwrite( STDERR, "Conflict fixture failed.\n" ); exit( 2 ); }
$before = (int) $wpdb->get_var( "SELECT COUNT(*) FROM {$wpdb->usermeta} WHERE meta_key IN ('cb_identity_managed','cb_identity_migration_run')" );
$blocked = $migration->apply( '22222222-2222-4222-8222-222222222222' );
$after = (int) $wpdb->get_var( "SELECT COUNT(*) FROM {$wpdb->usermeta} WHERE meta_key IN ('cb_identity_managed','cb_identity_migration_run')" );
$check( $blocked['status'] === 'CONFLICT' && $before === $after, 'collision fails closed without marker writes' );
$check( $migration->apply( 'not-a-run' )['status'] === 'UNSUPPORTED', 'invalid run identifier is rejected' );

echo "Checks: {$checks}; failures: {$failures}.\n";
if ( ! $failures ) { echo 'PASS synthetic reversible identity migration WP ' . $GLOBALS['wp_version'] . ' PHP ' . PHP_VERSION . "\n"; }
exit( $failures ? 1 : 0 );
