<?php
/** Requires an explicitly disposable MariaDB; no real WP data is touched. */
define( 'ABSPATH', __DIR__ . '/' );
$port = (int) getenv( 'CARMILLA_TOKEN_TEST_PORT' );
$prefix = getenv( 'CARMILLA_TOKEN_TEST_PREFIX' );
if ( $port < 1 || $port > 65535 || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) || getenv( 'CARMILLA_TOKEN_DISPOSABLE' ) !== 'yes' ) {
	fwrite( STDERR, "Disposable test flag, loopback port and random QA prefix required.\n" ); exit( 2 );
}
define( 'CB_JWT_SECRET', hash( 'sha256', 'synthetic-32-byte-master-only', true ) );
function wp_json_encode( $data ) { return json_encode( $data ); }
function get_site_url() { return 'https://qa.example.test/wp'; }
function get_user_meta( $id, $key, $single ) { return 0; }
class WP_User {
	public $ID; public $user_email; public $roles;
	public function __construct( int $id ) { $this->ID = $id; $this->user_email = 'synthetic@example.test'; $this->roles = array( 'customer' ); }
}
require_once dirname( __DIR__ ) . '/includes/class-cb-jwt.php';
require_once dirname( __DIR__ ) . '/includes/class-cb-web-access-token.php';
mysqli_report( MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT );
$checks = 0; $failures = 0;
function token_check( bool $result, string $label ): void {
	global $checks, $failures; ++$checks; if ( ! $result ) { ++$failures; }
	echo ( $result ? 'PASS ' : 'FAIL ' ) . $label . "\n";
}
function token_rejects( callable $fn ): bool { try { $fn(); return false; } catch ( Throwable $e ) { return true; } }
function token_b64( string $input ): string { return rtrim( strtr( base64_encode( $input ), '+/', '-_' ), '=' ); }
function token_sign_test( string $json, string $site, string $header = '{"alg":"HS256","typ":"JWT"}' ): string {
	$input = token_b64( $header ) . '.' . token_b64( $json );
	$key = hash_hmac( 'sha256', "carmilla-web-access-signing-v1\0" . $site, CB_JWT_SECRET, true );
	return $input . '.' . token_b64( hash_hmac( 'sha256', $input, $key, true ) );
}
try {
	$db = new mysqli( '127.0.0.1', 'synthetic_qa', 'synthetic_qa_password', 'carmilla_token_qa', $port );
	foreach ( CB_Web_Session_Store::schema_sql( $prefix ) as $sql ) { $db->query( $sql ); }
	$site = hash( 'sha256', "https://qa.example.test/wp\0" . '1' );
	$other_site = hash( 'sha256', "https://qa.example.test/other\0" . '1' );
	$now = 1800000000;
	$clock = static function () use ( &$now ) { return $now; };
	$passwords = array( 1 => 'synthetic-password-hash-one', 2 => 'synthetic-password-hash-two' );
	$password = static function ( int $id ) use ( &$passwords ) { return $passwords[$id] ?? null; };
	$store = new CB_Web_Session_Store( $db, $prefix, $site, hash( 'sha256', 'synthetic-fingerprint-key', true ), $password, $clock );
	$other_store = new CB_Web_Session_Store( $db, $prefix, $other_site, hash( 'sha256', 'synthetic-fingerprint-key', true ), $password, $clock );
	$tokens = new CB_Web_Access_Token( $store, $site, CB_JWT_SECRET, $clock );
	$other_tokens = new CB_Web_Access_Token( $other_store, $other_site, CB_JWT_SECRET, $clock );
	$a = $store->create( 1 ); $b = $store->create( 1 ); $other_user = $store->create( 2 );
	$access = $tokens->issue( 1, $a['family_id'] );
	token_check( $tokens->verify( $access ) === 1, 'active family issues and verifies browser access token' );
	$parts = explode( '.', $access );
	$body = base64_decode( strtr( $parts[1], '-_', '+/' ) );
	$claims = json_decode( $body, true );
	token_check( $claims['typ'] === 'carmilla_web_access' && $claims['site'] === $site && $claims['uid'] === 1 && $claims['family'] === $a['family_id'] && $claims['exp'] - $claims['iat'] === 900, 'web type site user family and bounded lifetime are bound' );
	token_check( strpos( $body, $a['credential'] ) === false && strpos( $body, 'password' ) === false && strpos( $body, '@' ) === false && ! isset( $claims['role'] ), 'claims exclude credentials password email and role' );
	token_check( $other_tokens->verify( $access ) === null, 'other site rejects token' );
	token_check( token_rejects( static function () use ( $store, $other_site ) { new CB_Web_Access_Token( $store, $other_site, CB_JWT_SECRET ); } ), 'mismatched store and site rejected' );
	token_check( token_rejects( static function () use ( $store, $site ) { new CB_Web_Access_Token( $store, $site, str_repeat( 'x', 32 ) ); } ), 'obviously invalid key rejected' );
	token_check( token_rejects( static function () use ( $store, $site ) { new CB_Web_Access_Token( $store, $site, 'short' ); } ), 'short key rejected' );
	token_check( token_rejects( static function () use ( $tokens, $a ) { $tokens->issue( 2, $a['family_id'] ); } ), 'family cannot issue for another user' );
	token_check( token_rejects( static function () use ( $tokens ) { $tokens->issue( 1, 'invalid' ); } ), 'malformed family cannot issue token' );
	$mobile_access = CB_JWT::issue( new WP_User( 1 ), 'access' );
	$mobile_refresh = CB_JWT::issue( new WP_User( 1 ), 'refresh' );
	token_check( CB_JWT::decode( $mobile_access )['typ'] === 'access' && CB_JWT::decode( $mobile_refresh )['typ'] === 'refresh', 'existing mobile access and refresh contract unchanged' );
	token_check( $tokens->verify( $mobile_access ) === null && $tokens->verify( $mobile_refresh ) === null, 'browser verifier rejects mobile tokens' );
	token_check( CB_JWT::decode( $access ) === null, 'mobile verifier rejects browser token even when master secrets match' );
	foreach ( array( '', 'x', 'a.b.c', $access . 'x', str_repeat( 'a', 2049 ), $parts[0] . '.' . $parts[1] . '.=', $parts[0] . '.' . $parts[1] . '.' . $parts[2] . '=' ) as $invalid ) {
		token_check( $tokens->verify( $invalid ) === null, 'malformed noncanonical or tampered token rejected' );
	}
	foreach ( array( 'typ' => 'access', 'site' => $other_site, 'uid' => '1', 'family' => str_repeat( 'f', 32 ), 'iat' => $now + 1, 'exp' => $now + 901 ) as $field => $value ) {
		$forged = $claims; $forged[$field] = $value;
		token_check( $tokens->verify( token_sign_test( json_encode( $forged ), $site ) ) === null, 'signed invalid claim rejected: ' . $field );
	}
	$forged = $claims; $forged['uid'] = 2;
	token_check( $tokens->verify( token_sign_test( json_encode( $forged ), $site ) ) === null, 'signed token cannot rebind family to another user' );
	$forged = $claims; $forged['role'] = 'ADMIN';
	token_check( $tokens->verify( token_sign_test( json_encode( $forged ), $site ) ) === null, 'extra role claim rejected' );
	$forged = $claims; unset( $forged['exp'] );
	token_check( $tokens->verify( token_sign_test( json_encode( $forged ), $site ) ) === null, 'missing expiry rejected' );
	$forged = str_replace( '"uid":1', '"uid":1,"uid":1', $body );
	token_check( $tokens->verify( token_sign_test( $forged, $site ) ) === null, 'duplicate JSON claims rejected even with a valid signature' );
	token_check( $tokens->verify( token_sign_test( $body, $site, '{"alg":"none","typ":"JWT"}' ) ) === null, 'wrong algorithm header rejected' );
	$allowed = true;
	$capability = static function ( int $id, string $cap ) use ( &$allowed ) { return $id === 1 && $cap === 'manage_options' && $allowed; };
	token_check( $tokens->authorize( $access, 'manage_options', $capability ), 'current capability callback can allow' );
	$allowed = false;
	token_check( ! $tokens->authorize( $access, 'manage_options', $capability ), 'revoked capability takes effect without changing token' );
	token_check( ! $tokens->authorize( $access, 'manage_options', static function () { throw new RuntimeException(); } ), 'capability failure denies access' );
	token_check( ! $tokens->authorize( $access, 'bad-cap!', $capability ), 'malformed capability denied' );
	token_check( ! $tokens->authorize( $access, 'manage_options', static function () { return 1; } ), 'non-boolean capability result cannot grant access' );
	$called = false;
	$should_not_run = static function () use ( &$called ) { $called = true; return true; };
	token_check( ! $tokens->authorize( 'invalid', 'manage_options', $should_not_run ) && ! $called, 'invalid token never reaches capability callback' );
	$now = 'bad-clock';
	token_check( $tokens->verify( $access ) === null && token_rejects( static function () use ( $tokens, $a ) { $tokens->issue( 1, $a['family_id'] ); } ), 'invalid clock denies verification and issuance' );
	$now = 1800000000;
	$now += 899; token_check( $tokens->verify( $access ) === 1, 'token remains valid immediately before expiry' );
	$now += 1; token_check( $tokens->verify( $access ) === null, 'token expires at 15 minutes' );
	$now -= 900;
	$rotated = $store->rotate( $a['credential'] );
	token_check( is_array( $rotated ) && $tokens->verify( $access ) === 1, 'ordinary rotation retains access family' );
	$store->rotate( $a['credential'] );
	token_check( $tokens->verify( $access ) === null, 'refresh replay immediately invalidates access token' );
	token_check( $tokens->verify( $tokens->issue( 1, $b['family_id'] ) ) === 1, 'replay does not revoke other device family' );
	$other_access = $tokens->issue( 2, $other_user['family_id'] );
	$store->revoke( $other_user['credential'] );
	token_check( $tokens->verify( $other_access ) === null, 'logout revokes access immediately' );
	$password_access = $tokens->issue( 1, $b['family_id'] ); $passwords[1] = 'synthetic-changed-hash';
	token_check( $tokens->verify( $password_access ) === null && token_rejects( static function () use ( $tokens, $b ) { $tokens->issue( 1, $b['family_id'] ); } ), 'password change invalidates access and future issuance' );
	$passwords[1] = 'synthetic-password-hash-one';
	$passwords[1] = array( 'unexpected' );
	token_check( $tokens->verify( $password_access ) === null && token_rejects( static function () use ( $tokens, $b ) { $tokens->issue( 1, $b['family_id'] ); } ), 'failed password provider denies verification and issuance' );
	unset( $passwords[1] );
	token_check( $tokens->verify( $password_access ) === null, 'deleted user cannot authenticate' );
	$passwords[1] = 'synthetic-password-hash-one';
	$failure_session = $store->create( 1 ); $failure_token = $tokens->issue( 1, $failure_session['family_id'] );
	$killer = new mysqli( '127.0.0.1', 'synthetic_qa', 'synthetic_qa_password', 'carmilla_token_qa', $port );
	$killer->kill( $db->thread_id );
	token_check( $tokens->verify( $failure_token ) === null, 'lost session database denies token' );
	token_check( token_rejects( static function () use ( $tokens, $failure_session ) { $tokens->issue( 1, $failure_session['family_id'] ); } ), 'lost session database prevents token issuance' );
	echo "Checks: {$checks}; failures: {$failures}.\n"; exit( $failures ? 1 : 0 );
} catch ( Throwable $e ) {
	fwrite( STDERR, 'Test infrastructure failed: ' . get_class( $e ) . "\n" ); exit( 2 );
}
