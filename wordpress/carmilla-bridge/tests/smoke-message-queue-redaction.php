<?php
/** Isolated queue handler probe. The fake adapter never contacts a provider. */
define( 'ABSPATH', __DIR__ );

class WP_Error {
	private $code;
	private $message;
	public function __construct( $code, $message ) {
		$this->code = $code;
		$this->message = $message;
	}
	public function get_error_code() { return $this->code; }
	public function get_error_message() { return $this->message; }
}
function is_wp_error( $value ) { return $value instanceof WP_Error; }
class CB_SMS_HTTP_Adapter {
	public static $result;
	public static function send( $to, $message ) { return self::$result; }
}
require __DIR__ . '/../includes/class-cb-message-queue.php';

$secret = 'fake-phone-09120000000-fake-token-XYZ';
$log_file = sys_get_temp_dir() . '/carmilla-queue-redaction-' . getmypid() . '.log';
ini_set( 'log_errors', '1' );
ini_set( 'error_log', $log_file );
$cases = array(
	array( 'retryable', 'provider_error', true, '' ),
	array( 'secret_code', $secret, true, '' ),
	array( 'invalid_config', 'invalid_config', false, 'invalid_config' ),
	array( 'missing_config', 'missing_config', false, 'missing_config' ),
	array( 'http_400', 'provider_400_' . $secret, false, 'http_400' ),
);
$failed = 0;
foreach ( $cases as $case ) {
	list( $name, $code, $retry, $safe_code ) = $case;
	@unlink( $log_file );
	CB_SMS_HTTP_Adapter::$result = new WP_Error( $code, $secret );
	$exception = '';
	try {
		CB_Message_Queue::process_sms( $secret, 'synthetic message' );
	} catch ( Exception $error ) {
		$exception = $error->getMessage();
	}
	$log = file_exists( $log_file ) ? file_get_contents( $log_file ) : '';
	$ok = ( $retry === ( '' !== $exception ) )
		&& false === strpos( $exception . $log, $secret )
		&& ( $retry ? '' === $log : false !== strpos( $log, $safe_code ) );
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . $name . ' retry=' . (int) $retry . ' log=' . trim( $log ) . ' exception=' . $exception . "\n";
	$failed += $ok ? 0 : 1;
}
@unlink( $log_file );
exit( $failed ? 1 : 0 );
