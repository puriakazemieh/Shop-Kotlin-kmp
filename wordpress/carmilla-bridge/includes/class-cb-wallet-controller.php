<?php
/**
 * Wallet endpoints — balance + transactions stored in user meta
 * (cb_wallet_balance / cb_wallet_txns), top-up via ZarinPal.
 *
 *   GET  api/wallet/balance         -> WalletBalanceResponse
 *   GET  api/wallet/transactions    -> PageResponse<WalletTransactionResponse>  (?page,&size)
 *   POST api/wallet/top-up          -> String (paymentUrl)   (TopUpRequest{amount})
 *   POST api/wallet/withdraw        -> 200                    (WithdrawRequest{amount,iban})
 *   GET  api/wallet/top-up/verify   -> 302 redirect to the app deep link
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Wallet_Controller {

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$login = array( 'CB_Plugin', 'require_login' );

		register_rest_route( $ns, '/api/wallet/balance', array(
			'methods' => 'GET', 'callback' => array( $this, 'balance' ), 'permission_callback' => $login,
		) );
		register_rest_route( $ns, '/api/wallet/transactions', array(
			'methods' => 'GET', 'callback' => array( $this, 'transactions' ), 'permission_callback' => $login,
		) );
		register_rest_route( $ns, '/api/wallet/top-up', array(
			'methods' => 'POST', 'callback' => array( $this, 'top_up' ), 'permission_callback' => $login,
		) );
		register_rest_route( $ns, '/api/wallet/withdraw', array(
			'methods' => 'POST', 'callback' => array( $this, 'withdraw' ), 'permission_callback' => $login,
		) );
		register_rest_route( $ns, '/api/wallet/top-up/verify', array(
			'methods' => 'GET', 'callback' => array( $this, 'verify_top_up' ), 'permission_callback' => '__return_true',
		) );
	}

	public function balance(): WP_REST_Response {
		$uid = get_current_user_id();
		return cb_response( array(
			'balance' => cb_wallet_balance( $uid ),
			'userId'  => $uid,
		) );
	}

	public function transactions( WP_REST_Request $request ): WP_REST_Response {
		$uid   = get_current_user_id();
		$page  = max( 0, (int) $request->get_param( 'page' ) );
		$size  = max( 1, (int) ( $request->get_param( 'size' ) ?: 20 ) );
		$all   = cb_wallet_txns( $uid );
		$total = count( $all );
		$slice = array_slice( $all, $page * $size, $size );
		$pages = (int) ceil( $total / $size );
		return cb_response( cb_page( array_values( $slice ), $page, $size, $total, $pages ) );
	}

	public function top_up( WP_REST_Request $request ): WP_REST_Response {
		$amount = (float) ( $request->get_json_params()['amount'] ?? 0 );
		if ( $amount <= 0 ) {
			return cb_error( 'مبلغ نامعتبر است', 400, 'INVALID_AMOUNT', 'api/wallet/top-up' );
		}
		if ( ! cb_zp_merchant() ) {
			return cb_error( 'درگاه پرداخت پیکربندی نشده است', 503, 'GATEWAY_NOT_CONFIGURED', 'api/wallet/top-up' );
		}
		$uid   = get_current_user_id();
		$token = wp_generate_password( 20, false );
		set_transient( 'cb_topup_' . $token, array( 'uid' => $uid, 'amount' => $amount ), HOUR_IN_SECONDS );

		$gw_amount = cb_zp_amount( $amount );
		$callback  = add_query_arg( 'token', $token, rest_url( CB_REST_NAMESPACE . '/api/wallet/top-up/verify' ) );
		$authority = cb_zp_request( $gw_amount, $callback, 'شارژ کیف پول' );
		if ( ! $authority ) {
			return cb_error( 'ایجاد تراکنش ناموفق بود', 502, 'GATEWAY_REJECTED', 'api/wallet/top-up' );
		}
		set_transient( 'cb_topup_auth_' . $token, array( 'authority' => $authority, 'amount' => $gw_amount ), HOUR_IN_SECONDS );

		// The app expects a bare paymentUrl string for top-up.
		return cb_response( cb_zp_startpay( $authority ) );
	}

	public function verify_top_up( WP_REST_Request $request ) {
		$token   = (string) $request->get_param( 'token' );
		$status  = (string) $request->get_param( 'Status' );
		$pending = get_transient( 'cb_topup_' . $token );
		$auth    = get_transient( 'cb_topup_auth_' . $token );

		if ( ! is_array( $pending ) || ! is_array( $auth ) ) {
			return $this->redirect( cb_app_return_url( array( 'wallet' => 'topup', 'status' => 'failed' ) ) );
		}
		if ( strtoupper( $status ) !== 'OK' ) {
			return $this->redirect( cb_app_return_url( array( 'wallet' => 'topup', 'status' => 'failed' ) ) );
		}

		$ref = cb_zp_verify( (int) $auth['amount'], (string) $auth['authority'] );
		if ( $ref !== null ) {
			cb_wallet_add( (int) $pending['uid'], (float) $pending['amount'], 'TOPUP', 'شارژ کیف پول', $ref );
			delete_transient( 'cb_topup_' . $token );
			delete_transient( 'cb_topup_auth_' . $token );
			return $this->redirect( cb_app_return_url( array( 'wallet' => 'topup', 'status' => 'success' ) ) );
		}
		return $this->redirect( cb_app_return_url( array( 'wallet' => 'topup', 'status' => 'failed' ) ) );
	}

	public function withdraw( WP_REST_Request $request ): WP_REST_Response {
		$body   = $request->get_json_params();
		$amount = (float) ( $body['amount'] ?? 0 );
		$iban   = sanitize_text_field( (string) ( $body['iban'] ?? '' ) );
		$uid    = get_current_user_id();
		if ( $amount <= 0 ) {
			return cb_error( 'مبلغ نامعتبر است', 400, 'INVALID_AMOUNT', 'api/wallet/withdraw' );
		}
		if ( $amount > cb_wallet_balance( $uid ) ) {
			return cb_error( 'موجودی کافی نیست', 400, 'INSUFFICIENT_BALANCE', 'api/wallet/withdraw' );
		}
		// Debit now and record a pending withdrawal for the admin to process.
		cb_wallet_add( $uid, -$amount, 'WITHDRAW', 'درخواست برداشت به ' . $iban, $iban );
		return cb_response( null, 200 );
	}

	private function redirect( string $url ) {
		return new WP_REST_Response( null, 302, array( 'Location' => $url ) );
	}
}
