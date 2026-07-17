<?php
/**
 * Cart endpoints — a per-user server cart stored in user meta (cb_cart), shaped
 * to the app's CartResponse. Kept independent of the WooCommerce session cart so
 * the headless (JWT) client gets a stable, DTO-exact contract.
 *
 * All routes require a logged-in user (Bearer token).
 *
 *   GET    api/cart                                  -> CartResponse
 *   DELETE api/cart                                  -> 200 (cleared)
 *   POST   api/cart/items                            -> CartResponse   (AddCartItemRequest)
 *   PATCH  api/cart/items/{itemId}                   -> CartResponse   (UpdateCartItemRequest{qty})
 *   DELETE api/cart/items/{itemId}                   -> CartResponse
 *   PUT    api/cart/items/{variantId}                -> CartResponse   (SetCartVariantQtyRequest{qty})
 *   PATCH  api/cart/items/{variantId}/adjust         -> CartResponse   (AdjustCartVariantQtyRequest{delta})
 *   POST   api/cart/items/{itemId}/save-for-later    -> CartResponse
 *   POST   api/cart/items/{itemId}/move-to-cart      -> CartResponse
 *   POST   api/cart/discount                         -> CartResponse   (ApplyDiscountRequest{code})
 *   DELETE api/cart/discount                         -> CartResponse
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Cart_Controller {

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$login = array( 'CB_Plugin', 'require_login' );

		register_rest_route( $ns, '/api/cart', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'get_cart' ), 'permission_callback' => $login ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'clear' ), 'permission_callback' => $login ),
		) );

		register_rest_route( $ns, '/api/cart/items', array(
			'methods'             => 'POST',
			'callback'            => array( $this, 'add_item' ),
			'permission_callback' => $login,
		) );

		// {id} handles itemId (PATCH/DELETE) and variantId (PUT) — disambiguated by method.
		register_rest_route( $ns, '/api/cart/items/(?P<id>\d+)', array(
			array( 'methods' => 'PATCH', 'callback' => array( $this, 'update_qty' ), 'permission_callback' => $login ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'remove' ), 'permission_callback' => $login ),
			array( 'methods' => 'PUT', 'callback' => array( $this, 'set_variant_qty' ), 'permission_callback' => $login ),
		) );

		register_rest_route( $ns, '/api/cart/items/(?P<id>\d+)/adjust', array(
			'methods'             => 'PATCH',
			'callback'            => array( $this, 'adjust_variant_qty' ),
			'permission_callback' => $login,
		) );

		register_rest_route( $ns, '/api/cart/items/(?P<id>\d+)/save-for-later', array(
			'methods'             => 'POST',
			'callback'            => array( $this, 'save_for_later' ),
			'permission_callback' => $login,
		) );

		register_rest_route( $ns, '/api/cart/items/(?P<id>\d+)/move-to-cart', array(
			'methods'             => 'POST',
			'callback'            => array( $this, 'move_to_cart' ),
			'permission_callback' => $login,
		) );

		register_rest_route( $ns, '/api/cart/discount', array(
			array( 'methods' => 'POST', 'callback' => array( $this, 'apply_discount' ), 'permission_callback' => $login ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'remove_discount' ), 'permission_callback' => $login ),
		) );
	}

	private function uid(): int {
		return get_current_user_id();
	}

	private function ok(): WP_REST_Response {
		return cb_response( cb_cart_response( $this->uid() ) );
	}

	public function get_cart(): WP_REST_Response {
		return $this->ok();
	}

	public function clear(): WP_REST_Response {
		cb_save_cart_lines( $this->uid(), array() );
		delete_user_meta( $this->uid(), 'cb_cart_coupon' );
		return $this->ok();
	}

	public function add_item( WP_REST_Request $request ): WP_REST_Response {
		if ( ! cb_woo_active() ) {
			return cb_error( 'WooCommerce غیرفعال است', 400, 'WOO_INACTIVE', 'api/cart/items' );
		}
		$body       = $request->get_json_params();
		$qty        = max( 1, (int) ( $body['qty'] ?? 1 ) );
		$variant_id = (int) ( $body['variantId'] ?? 0 );
		$product_id = (int) ( $body['productId'] ?? 0 );

		// Resolve variantId: explicit wins; else a simple product's id is its variant.
		if ( ! $variant_id && $product_id ) {
			$p = wc_get_product( $product_id );
			if ( $p && $p->is_type( 'variable' ) ) {
				return cb_error( 'برای محصولِ متغیر باید variantId بفرستید', 400, 'VARIANT_REQUIRED', 'api/cart/items' );
			}
			$variant_id = $product_id;
		}
		if ( ! cb_variant_parts( $variant_id ) ) {
			return cb_error( 'محصول یافت نشد', 404, 'NOT_FOUND', 'api/cart/items' );
		}

		$uid   = $this->uid();
		$lines = cb_cart_lines( $uid );
		$merged = false;
		foreach ( $lines as &$line ) {
			if ( (int) $line['variantId'] === $variant_id && empty( $line['saved'] ) ) {
				$line['qty'] = (int) $line['qty'] + $qty;
				$merged      = true;
				break;
			}
		}
		unset( $line );
		if ( ! $merged ) {
			$lines[] = array(
				'id'        => cb_cart_next_id( $uid ),
				'variantId' => $variant_id,
				'qty'       => $qty,
				'saved'     => false,
			);
		}
		cb_save_cart_lines( $uid, $lines );
		return $this->ok();
	}

	public function update_qty( WP_REST_Request $request ): WP_REST_Response {
		$item_id = (int) $request['id'];
		$qty     = max( 1, (int) ( $request->get_json_params()['qty'] ?? 1 ) );
		return $this->mutate_by_line_id( $item_id, function ( &$line ) use ( $qty ) {
			$line['qty'] = $qty;
		} );
	}

	public function remove( WP_REST_Request $request ): WP_REST_Response {
		$item_id = (int) $request['id'];
		$uid     = $this->uid();
		$lines   = array_filter( cb_cart_lines( $uid ), function ( $l ) use ( $item_id ) {
			return (int) $l['id'] !== $item_id;
		} );
		cb_save_cart_lines( $uid, $lines );
		return $this->ok();
	}

	public function set_variant_qty( WP_REST_Request $request ): WP_REST_Response {
		$variant_id = (int) $request['id'];
		$qty        = max( 0, (int) ( $request->get_json_params()['qty'] ?? 0 ) );
		$uid        = $this->uid();
		$lines      = cb_cart_lines( $uid );
		$found      = false;
		foreach ( $lines as &$line ) {
			if ( (int) $line['variantId'] === $variant_id && empty( $line['saved'] ) ) {
				$line['qty'] = $qty;
				$found       = true;
				break;
			}
		}
		unset( $line );
		if ( ! $found && $qty > 0 && cb_variant_parts( $variant_id ) ) {
			$lines[] = array( 'id' => cb_cart_next_id( $uid ), 'variantId' => $variant_id, 'qty' => $qty, 'saved' => false );
		}
		// A zero quantity removes the line.
		$lines = array_filter( $lines, function ( $l ) {
			return (int) $l['qty'] > 0;
		} );
		cb_save_cart_lines( $uid, $lines );
		return $this->ok();
	}

	public function adjust_variant_qty( WP_REST_Request $request ): WP_REST_Response {
		$variant_id = (int) $request['id'];
		$delta      = (int) ( $request->get_json_params()['delta'] ?? 0 );
		$uid        = $this->uid();
		$lines      = cb_cart_lines( $uid );
		$found      = false;
		foreach ( $lines as &$line ) {
			if ( (int) $line['variantId'] === $variant_id && empty( $line['saved'] ) ) {
				$line['qty'] = (int) $line['qty'] + $delta;
				$found       = true;
				break;
			}
		}
		unset( $line );
		if ( ! $found && $delta > 0 && cb_variant_parts( $variant_id ) ) {
			$lines[] = array( 'id' => cb_cart_next_id( $uid ), 'variantId' => $variant_id, 'qty' => $delta, 'saved' => false );
		}
		$lines = array_filter( $lines, function ( $l ) {
			return (int) $l['qty'] > 0;
		} );
		cb_save_cart_lines( $uid, $lines );
		return $this->ok();
	}

	public function save_for_later( WP_REST_Request $request ): WP_REST_Response {
		return $this->mutate_by_line_id( (int) $request['id'], function ( &$line ) {
			$line['saved'] = true;
		} );
	}

	public function move_to_cart( WP_REST_Request $request ): WP_REST_Response {
		return $this->mutate_by_line_id( (int) $request['id'], function ( &$line ) {
			$line['saved'] = false;
		} );
	}

	public function apply_discount( WP_REST_Request $request ): WP_REST_Response {
		$code = trim( (string) ( $request->get_json_params()['code'] ?? '' ) );
		if ( $code === '' ) {
			return cb_error( 'کد تخفیف خالی است', 400, 'EMPTY_CODE', 'api/cart/discount' );
		}
		if ( cb_woo_active() && class_exists( 'WC_Coupon' ) ) {
			$coupon = new WC_Coupon( $code );
			if ( ! $coupon->get_id() ) {
				return cb_error( 'کد تخفیف نامعتبر است', 404, 'INVALID_COUPON', 'api/cart/discount' );
			}
		}
		update_user_meta( $this->uid(), 'cb_cart_coupon', $code );
		return $this->ok();
	}

	public function remove_discount(): WP_REST_Response {
		delete_user_meta( $this->uid(), 'cb_cart_coupon' );
		return $this->ok();
	}

	/**
	 * Apply a mutation to the line with the given id and persist.
	 */
	private function mutate_by_line_id( int $item_id, callable $fn ): WP_REST_Response {
		$uid   = $this->uid();
		$lines = cb_cart_lines( $uid );
		foreach ( $lines as &$line ) {
			if ( (int) $line['id'] === $item_id ) {
				$fn( $line );
				break;
			}
		}
		unset( $line );
		cb_save_cart_lines( $uid, $lines );
		return $this->ok();
	}
}
