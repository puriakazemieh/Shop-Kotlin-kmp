<?php
/**
 * Payment endpoints — ZarinPal gateway (via cb_zp_* helpers). The app calls
 * api/payment/request with an orderId, gets back a paymentUrl, opens it in a
 * browser/WebView, and the gateway returns to api/payment/verify which
 * finalizes the order and redirects to the app's result deep link.
 *
 *   POST api/payment/request   -> PaymentResponse{ paymentUrl }   (PaymentRequest{orderId})
 *   GET  api/payment/verify    -> 302 redirect to the app's result deep link
 *
 * Configure in wp-admin (option keys): cb_zarinpal_merchant, cb_zarinpal_sandbox,
 * cb_app_return_url (deep link base, e.g. carmilla://payment/result).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Payment_Controller {

	public function register_routes(): void {
		$ns = CB_REST_NAMESPACE;

		register_rest_route( $ns, '/api/payment/request', array(
			'methods'             => 'POST',
			'callback'            => array( $this, 'request_payment' ),
			'permission_callback' => array( 'CB_Plugin', 'require_login' ),
		) );

		// The gateway redirects the browser here; no auth (identified by Authority).
		register_rest_route( $ns, '/api/payment/verify', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'verify_payment' ),
			'permission_callback' => '__return_true',
		) );
	}

	public function request_payment( WP_REST_Request $request ): WP_REST_Response {
		if ( ! cb_woo_active() ) {
			return cb_error( 'WooCommerce غیرفعال است', 400, 'WOO_INACTIVE', 'api/payment/request' );
		}
		if ( ! cb_zp_merchant() ) {
			return cb_error( 'درگاه پرداخت پیکربندی نشده است', 503, 'GATEWAY_NOT_CONFIGURED', 'api/payment/request' );
		}
		$order_id = (int) ( $request->get_json_params()['orderId'] ?? 0 );
		$order    = wc_get_order( $order_id );
		if ( ! $order || (int) $order->get_customer_id() !== get_current_user_id() ) {
			return cb_error( 'سفارش یافت نشد', 404, 'NOT_FOUND', 'api/payment/request' );
		}
		if ( $order->is_paid() ) {
			return cb_error( 'این سفارش قبلاً پرداخت شده است', 409, 'ALREADY_PAID', 'api/payment/request' );
		}

		$due = (float) $order->get_total();
		if ( $due <= 0 ) {
			$order->payment_complete();
			$order->update_status( 'processing' );
			return cb_response( array( 'paymentUrl' => cb_app_return_url( array( 'order' => $order_id, 'status' => 'success' ) ) ) );
		}

		$amount    = cb_zp_amount( $due );
		$callback  = add_query_arg( 'order', $order_id, rest_url( CB_REST_NAMESPACE . '/api/payment/verify' ) );
		$authority = cb_zp_request( $amount, $callback, 'سفارش #' . $order_id );
		if ( ! $authority ) {
			return cb_error( 'ایجاد تراکنش ناموفق بود', 502, 'GATEWAY_REJECTED', 'api/payment/request' );
		}

		$order->update_meta_data( '_cb_zp_authority', $authority );
		$order->update_meta_data( '_cb_zp_amount', $amount );
		$order->save();

		return cb_response( array( 'paymentUrl' => cb_zp_startpay( $authority ) ) );
	}

	public function verify_payment( WP_REST_Request $request ) {
		$order_id  = (int) $request->get_param( 'order' );
		$status    = (string) $request->get_param( 'Status' );
		$authority = (string) $request->get_param( 'Authority' );
		$order     = wc_get_order( $order_id );

		if ( ! $order ) {
			return $this->redirect( cb_app_return_url( array( 'order' => $order_id, 'status' => 'failed' ) ) );
		}
		if ( $order->is_paid() ) {
			return $this->redirect( cb_app_return_url( array( 'order' => $order_id, 'status' => 'success' ) ) );
		}
		if ( strtoupper( $status ) !== 'OK' ) {
			$order->update_status( 'failed', 'لغو پرداخت توسط کاربر یا بانک.' );
			return $this->redirect( cb_app_return_url( array( 'order' => $order_id, 'status' => 'failed' ) ) );
		}

		$saved_authority = (string) $order->get_meta( '_cb_zp_authority' );
		if ( ! $saved_authority || $saved_authority !== $authority ) {
			$order->update_status( 'failed', 'خطای امنیتی: عدم تطابق شناسه پرداخت.' );
			return $this->redirect( cb_app_return_url( array( 'order' => $order_id, 'status' => 'failed' ) ) );
		}

		$expected_amount = cb_zp_amount( (float) $order->get_total() );
		$saved_amount    = (int) $order->get_meta( '_cb_zp_amount' );
		if ( $saved_amount !== $expected_amount ) {
			$order->update_status( 'failed', 'خطای امنیتی: تغییر مبلغ سفارش.' );
			return $this->redirect( cb_app_return_url( array( 'order' => $order_id, 'status' => 'failed' ) ) );
		}

		$ref = cb_zp_verify( $expected_amount, $authority );
		if ( $ref !== null ) {
			$order->payment_complete( $ref );
			$order->add_order_note( 'پرداخت ZarinPal موفق. کد پیگیری: ' . $ref );
			return $this->redirect( cb_app_return_url( array( 'order' => $order_id, 'status' => 'success' ) ) );
		}

		$order->update_status( 'failed', 'تأیید پرداخت ناموفق بود' );
		return $this->redirect( cb_app_return_url( array( 'order' => $order_id, 'status' => 'failed' ) ) );
	}

	private function redirect( string $url ) {
		return new WP_REST_Response( null, 302, array( 'Location' => $url ) );
	}
}
