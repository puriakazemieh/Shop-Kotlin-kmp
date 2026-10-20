<?php
/** Isolated guarded browser registration. It registers no route or hook. */
if ( ! defined( 'ABSPATH' ) ) { exit; }
require_once __DIR__ . '/class-cb-web-auth-login.php';
require_once __DIR__ . '/class-cb-identity-supported-writer.php';

final class CB_Web_Browser_Registration {
	private $policy;
	private $issue;
	private $writer;

	/** $issue receives a verified WP_User and returns CB_Web_Auth_Result. */
	public function __construct( CB_Web_Request_Policy $policy, callable $issue, ?CB_Identity_Supported_Writer $writer = null ) {
		$this->policy = $policy;
		$this->issue = $issue;
		$this->writer = $writer ?: new CB_Identity_Supported_Writer();
	}

	public function register( string $method, array $headers, array $server, array $input ): CB_Web_Auth_Result {
		if ( ! $this->policy->allows_post( $method, $headers, $server ) ) { return new CB_Web_Auth_Result( 403, array( 'error' => 'request_denied' ) ); }
		$email = $input['email'] ?? ''; $mobile = $input['mobile'] ?? ''; $password = $input['password'] ?? null;
		if ( ! is_string( $email ) || ! is_string( $mobile ) || ! is_string( $password ) || strlen( $email ) > 191 || strlen( $mobile ) > 191 || strlen( $password ) < 6 || strlen( $password ) > 4096 ) {
			return new CB_Web_Auth_Result( 400, array( 'error' => 'invalid_input' ) );
		}
		$email = trim( $email ); $mobile = trim( $mobile );
		if ( ( $email === '' && $mobile === '' ) || ( $email !== '' && ! is_email( $email ) ) ) { return new CB_Web_Auth_Result( 400, array( 'error' => 'invalid_input' ) ); }
		$user_id = $this->writer->create_customer( $email !== '' ? $email : $mobile, $email, $mobile, $password );
		if ( is_wp_error( $user_id ) ) {
			$code = $user_id->get_error_code();
			if ( $code === 'cb_identity_exists' ) { return new CB_Web_Auth_Result( 409, array( 'error' => 'identity_unavailable' ) ); }
			return new CB_Web_Auth_Result( $code === 'cb_identity_invalid' ? 400 : 503, array( 'error' => 'registration_unavailable' ) );
		}
		$user = get_user_by( 'id', (int) $user_id );
		if ( ! $user instanceof WP_User || ! $user->exists() ) { return new CB_Web_Auth_Result( 503, array( 'error' => 'account_recovery_required' ) ); }
		try { $result = call_user_func( $this->issue, $user ); } catch ( Throwable $e ) { $result = null; }
		if ( ! $result instanceof CB_Web_Auth_Result || $result->status() !== 200 || $result->cookie() === null ) {
			// The account may be committed and WordPress callbacks may already have
			// run. Never delete it or claim rollback; never issue credentials.
			update_user_meta( (int) $user_id, 'cb_identity_writer_state', 'needs_recovery' );
			return new CB_Web_Auth_Result( 503, array( 'error' => 'account_recovery_required' ) );
		}
		return new CB_Web_Auth_Result( 201, $result->body(), $result->cookie() );
	}
}
