<?php
define( 'ABSPATH', __DIR__ . '/' );
function __( $text ) { return $text; }
function add_action() {}
$theme_mod_calls = 0;
function get_theme_mod( $name, $default = false ) {
	global $theme_mod_calls;
	$theme_mod_calls++;
	return false;
}
$manifest = array( 'content.blog' => true, 'commerce.core' => true, 'academy.core' => false );
function get_option( $name, $default = false ) {
	global $manifest;
	return $name === 'cb_manifest_features' ? $manifest : $default;
}
require dirname( __DIR__ ) . '/inc/customizer.php';

$fail = 0;
function check( $condition, $label ) {
	global $fail;
	echo ( $condition ? 'PASS' : 'FAIL' ) . "  $label\n";
	if ( ! $condition ) { $fail++; }
}
check( carmilla_feature_enabled( 'blog' ), 'Blog follows manifest option' );
check( carmilla_feature_enabled( 'shop' ), 'Shop follows manifest option' );
check( ! carmilla_feature_enabled( 'courses' ), 'Disabled academy stays hidden' );
check( $theme_mod_calls === 0, 'Theme Mods are not read for feature visibility' );
$manifest['content.blog'] = false;
$manifest['academy.core'] = true;
check( ! carmilla_feature_enabled( 'courses' ), 'Dependency failure is fail-closed' );
echo $fail === 0 ? "ALL PASSED\n" : "$fail FAILED\n";
exit( $fail === 0 ? 0 : 1 );
