<?php
define( 'ABSPATH', __DIR__ . '/' );
$options = array( 'cb_manifest_features' => null, 'cb_legacy_migration_version' => 0 );
$theme_mods = array( 'carmilla_enable_shop' => false, 'carmilla_enable_blog' => true );
function get_option( $name, $default = false ) { global $options; return array_key_exists( $name, $options ) ? $options[ $name ] : $default; }
function update_option( $name, $value ) { global $options; $options[ $name ] = $value; return true; }
function get_theme_mod( $name, $default = null ) { global $theme_mods; return array_key_exists( $name, $theme_mods ) ? $theme_mods[ $name ] : $default; }
require dirname( __DIR__ ) . '/includes/class-cb-legacy-migration.php';

$fail = 0;
function check( $condition, $label ) {
	global $fail;
	echo ( $condition ? 'PASS' : 'FAIL' ) . "  $label\n";
	if ( ! $condition ) { $fail++; }
}
$plan = CB_Legacy_Migration::plan( array( 'shop' => false, 'blog' => true, 'unknown' => true ) );
check( $plan === array( 'commerce.core' => false, 'content.blog' => true ), 'Legacy mapping is allowlisted' );
$first = CB_Legacy_Migration::run();
check( $first['status'] === 'migrated', 'First run migrates legacy flags' );
check( $options['cb_manifest_features']['commerce.core'] === false, 'Disabled shop flag is preserved' );
$second = CB_Legacy_Migration::run();
check( $second['status'] === 'already_migrated', 'Second run is idempotent' );
echo $fail === 0 ? "ALL PASSED\n" : "$fail FAILED\n";
exit( $fail === 0 ? 0 : 1 );
