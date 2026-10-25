<?php
/** Disposable installed WordPress only. Does not print identities or credentials. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';

$checks = 0;
$failures = 0;
$check = static function ( bool $ok, string $label ) use ( &$checks, &$failures ) {
	++$checks;
	if ( ! $ok ) { ++$failures; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n";
};
$suffix = substr( hash( 'sha256', microtime( true ) . random_int( 1, PHP_INT_MAX ) ), 0, 12 );
$email = 'error-contract-' . $suffix . '@example.test';
$controller = new CB_Auth_Controller();

$request = static function ( array $values ): WP_REST_Request {
	$request = new WP_REST_Request( 'POST' );
	foreach ( $values as $key => $value ) { $request->set_param( $key, $value ); }
	return $request;
};
$error_code = static function ( $response ): string {
	$data = $response->get_data();
	return is_array( $data ) ? (string) ( $data['errorCode'] ?? '' ) : '';
};

$weak = $controller->register( $request( array( 'email' => $email, 'password' => 'short' ) ) );
$check( $weak->get_status() === 400 && $error_code( $weak ) === 'WEAK_PASSWORD', 'weak password has a distinct safe error code' );

$invalid = $controller->register( $request( array( 'email' => 'not-an-email', 'password' => 'synthetic-password' ) ) );
$check( $invalid->get_status() === 400 && $error_code( $invalid ) === 'INVALID_INPUT', 'invalid email has a distinct safe error code' );

$created = $controller->register( $request( array( 'email' => $email, 'password' => 'synthetic-password' ) ) );
$created_data = $created->get_data();
$created_id = is_array( $created_data ) && isset( $created_data['user']['id'] ) ? (int) $created_data['user']['id'] : 0;
$check( $created->get_status() === 201 && $created_id > 0, 'synthetic account can be created for duplicate check' );

$duplicate = $controller->register( $request( array( 'email' => $email, 'password' => 'synthetic-password' ) ) );
$check( $duplicate->get_status() === 409 && $error_code( $duplicate ) === 'EMAIL_ALREADY_EXISTS', 'duplicate email has a distinct safe error code' );

if ( $created_id > 0 ) {
	require_once ABSPATH . 'wp-admin/includes/user.php';
	wp_delete_user( $created_id );
}

echo "Checks: {$checks}; failures: {$failures}.\n";
exit( $failures ? 1 : 0 );
