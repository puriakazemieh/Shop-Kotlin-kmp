<?php
/** Operator-only CLI entrypoint. Never invoked by activation or web requests. */
if ( PHP_SAPI !== 'cli' ) { http_response_code( 404 ); exit( 2 ); }
$root = getenv( 'CARMILLA_WP_ROOT' ) ?: dirname( __DIR__, 4 );
if ( ! is_file( $root . '/wp-load.php' ) ) { fwrite( STDERR, "WordPress root unavailable.\n" ); exit( 2 ); }
require_once $root . '/wp-load.php';
require_once __DIR__ . '/../includes/class-cb-user-integrity.php';
require_once __DIR__ . '/../includes/class-cb-user-integrity-migration.php';

/** $tables is for isolated disposable tests; the direct CLI invocation always uses actual WP tables. */
function cb_user_integrity_command( array $args, ?array $tables = null ): array {
	$mode = $args[1] ?? '';
	if ( ! in_array( $mode, array( 'preflight', 'install' ), true ) ) {
		return array( 'code' => 2, 'message' => 'Usage: php user-integrity.php preflight | install <absolute-backup-file> <sha256>' );
	}
	$state = CB_User_Integrity::inspect( $tables );
	if ( $mode === 'preflight' ) {
		return array( 'code' => in_array( $state['state'], array( 'uninstalled', 'installed' ), true ) ? 0 : 3, 'message' => wp_json_encode( $state ) );
	}
	if ( $state['state'] === 'installed' ) { return array( 'code' => 0, 'message' => 'Already installed; no DDL.' ); }
	if ( $state['state'] !== 'uninstalled' ) { return array( 'code' => 3, 'message' => "Migration refused: {$state['state']}:{$state['reason']}." ); }
	$backup = $args[2] ?? '';
	$digest = strtolower( $args[3] ?? '' );
	if ( ! is_string( $backup ) || $backup === '' || ! is_file( $backup ) || ! is_readable( $backup ) || filesize( $backup ) < 1 || ! preg_match( '/^[a-f0-9]{64}$/D', $digest ) || ! hash_equals( $digest, hash_file( 'sha256', $backup ) ) ) {
		return array( 'code' => 3, 'message' => 'Migration refused: readable nonempty backup and matching SHA-256 are required.' );
	}
	$result = CB_User_Integrity_Migration::install( $tables );
	return $result['status'] === 'installed'
		? array( 'code' => 0, 'message' => wp_json_encode( $result ) )
		: array( 'code' => 4, 'message' => wp_json_encode( $result ) . ' DDL is not transactional; inspect partial state and restore the verified backup before retrying.' );
}

if ( realpath( $_SERVER['SCRIPT_FILENAME'] ?? '' ) === realpath( __FILE__ ) ) {
	$result = cb_user_integrity_command( $argv );
	$stream = $result['code'] === 0 ? STDOUT : STDERR;
	fwrite( $stream, $result['message'] . "\n" );
	exit( $result['code'] );
}
