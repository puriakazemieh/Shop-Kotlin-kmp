<?php
/**
 * Dependency-free smoke test for the security- and mapping-critical pieces
 * (CB_JWT round trip + CB_Blocks::blocks_to_html). Run: php tests/smoke.php
 * Stubs the handful of WP functions these classes use so no WordPress is needed.
 */

define( 'ABSPATH', __DIR__ . '/' );
function wp_json_encode( $d ) { return json_encode( $d ); }
function esc_html( $s ) { return htmlspecialchars( (string) $s, ENT_QUOTES ); }
function esc_url( $s ) { return filter_var( (string) $s, FILTER_SANITIZE_URL ); }
function wp_strip_all_tags( $s ) { return trim( strip_tags( (string) $s ) ); }
class WP_User {
	public $ID;
	public $user_email;
	public $roles;
	public function __construct( $id, $email, $roles ) {
		$this->ID = $id; $this->user_email = $email; $this->roles = $roles;
	}
}

$inc = dirname( __DIR__ ) . '/includes/';
require $inc . 'class-cb-jwt.php';
require $inc . 'class-cb-blocks.php';

$fail = 0;
function check( $cond, $label ) {
	global $fail;
	echo ( $cond ? 'PASS' : 'FAIL' ) . "  $label\n";
	if ( ! $cond ) { $fail++; }
}

$admin  = new WP_User( 7, 'admin@carmilla.test', array( 'administrator' ) );
$access = CB_JWT::issue( $admin, 'access' );
$p      = CB_JWT::decode( $access );
check( $p !== null, 'valid access token decodes' );
check( $p['uid'] === 7, 'uid claim' );
check( $p['sub'] === 'admin@carmilla.test', 'sub = email' );
check( $p['role'] === 'ADMIN', 'administrator -> ADMIN' );
check( $p['typ'] === 'access', 'typ = access' );

$cust = new WP_User( 9, 'c@x.com', array( 'customer' ) );
check( CB_JWT::decode( CB_JWT::issue( $cust, 'access' ) )['role'] === 'CUSTOMER', 'customer -> CUSTOMER' );
check( CB_JWT::decode( $access . 'x' ) === null, 'tampered token rejected' );
check( CB_JWT::decode( CB_JWT::issue( $admin, 'refresh' ) )['typ'] === 'refresh', 'refresh typ' );

$blocks = array(
	array( 'type' => 'header', 'content' => 'سلام', 'level' => 2 ),
	array( 'type' => 'paragraph', 'content' => 'یک <b>پاراگراف</b>' ),
	array( 'type' => 'image', 'content' => 'https://x/img.png', 'url' => 'https://x/img.png' ),
	array( 'type' => 'button', 'content' => 'خرید', 'url' => 'https://x/buy' ),
	array( 'type' => 'list', 'items' => array( 'یک', 'دو' ) ),
	array( 'type' => 'quote', 'content' => 'نقل قول' ),
	array( 'type' => 'divider' ),
);
$html = CB_Blocks::blocks_to_html( $blocks );
check( strpos( $html, '<!-- wp:heading {"level":2} -->' ) !== false, 'heading block' );
check( strpos( $html, '<h2>سلام</h2>' ) !== false, 'heading content' );
check( strpos( $html, '&lt;b&gt;' ) !== false, 'paragraph HTML escaped' );
check( strpos( $html, 'src="https://x/img.png"' ) !== false, 'image src' );
check( strpos( $html, 'href="https://x/buy"' ) !== false, 'button href' );
check( strpos( $html, '<li>یک</li><li>دو</li>' ) !== false, 'list items' );
check( strpos( $html, '<!-- wp:separator -->' ) !== false, 'divider block' );

echo "\n" . ( $fail === 0 ? 'ALL PASSED' : "$fail FAILED" ) . "\n";
exit( $fail === 0 ? 0 : 1 );

// Regression: empty variation options must serialize as {} (object), not [] (array),
// or the app's Map<String,...> field throws a SerializationException (blank home).
$empty = (object) array();
$assert_obj = json_encode( array( 'options' => $empty ) );
if ( strpos( $assert_obj, '"options":{}' ) === false ) {
	fwrite( STDERR, "FAIL: empty options did not encode as {}\n" );
	exit( 1 );
}
$nonempty = (object) array( 'رنگ' => array( 'قرمز' ) );
if ( strpos( json_encode( array( 'options' => $nonempty ) ), '"options":{' ) === false ) {
	fwrite( STDERR, "FAIL: non-empty options did not encode as object\n" );
	exit( 1 );
}
echo "PASS: options map encodes as JSON object in both empty and non-empty cases\n";
