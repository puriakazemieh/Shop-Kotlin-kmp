<?php
/** Local-only OTP sink for disposable browser QA. Never activates off localhost. */
if ( ! defined( 'ABSPATH' ) ) { exit; }

final class CB_Local_QA_OTP_Sink {
	private const OPTION = 'cb_local_qa_otp_sink';
	private const PREFIX = 'cb_local_qa_otp_';

	public static function boot(): void {
		if ( ! self::is_local() || ! get_option( self::OPTION, false ) ) { return; }
		add_filter( 'cb_send_otp_override', array( __CLASS__, 'capture' ), 10, 4 );
		add_action( 'admin_menu', array( __CLASS__, 'menu' ), 30 );
	}

	public static function capture( $override, $mobile, $code, $purpose ) {
		if ( ! is_string( $mobile ) || ! is_string( $code ) || ! is_string( $purpose ) || ! preg_match( '/\A[0-9]{6}\z/', $code ) ) { return new WP_Error( 'cb_local_otp_invalid', 'OTP test sink unavailable.' ); }
		set_transient( self::PREFIX . md5( $mobile . '_' . $purpose ), array( 'code' => $code, 'purpose' => $purpose, 'at' => time() ), 5 * MINUTE_IN_SECONDS );
		return true;
	}

	public static function menu(): void {
		add_submenu_page( 'carmilla-settings', 'کد آزمایشی ورود', 'کد آزمایشی ورود', 'manage_options', 'carmilla-local-otp', array( __CLASS__, 'page' ) );
	}

	public static function page(): void {
		if ( ! current_user_can( 'manage_options' ) ) { wp_die( esc_html__( 'دسترسی ندارید.', 'carmilla-bridge' ) ); }
		$items = array();
		// The sink deliberately has no index of mobile values. An administrator
		// supplies the synthetic number they just used, preventing an account list.
		$mobile = isset( $_GET['mobile'] ) && is_string( $_GET['mobile'] ) ? trim( wp_unslash( $_GET['mobile'] ) ) : '';
		if ( $mobile !== '' && check_admin_referer( 'cb_local_otp_view' ) ) {
			$item = get_transient( self::PREFIX . md5( $mobile . '_login' ) );
			if ( is_array( $item ) && isset( $item['code'] ) && is_string( $item['code'] ) ) { $items[] = $item['code']; }
		}
		echo '<div class="wrap"><h1>کد آزمایشی ورود</h1><p>فقط برای وردپرس محلی و شمارهٔ ساختگی است.</p><form method="get"><input type="hidden" name="page" value="carmilla-local-otp" />';
		wp_nonce_field( 'cb_local_otp_view' );
		echo '<label>شمارهٔ ساختگی <input name="mobile" value="' . esc_attr( $mobile ) . '" /></label> <button class="button button-primary">نمایش کد</button></form>';
		if ( $mobile !== '' ) { echo '<p>کد: <strong>' . ( $items ? esc_html( $items[0] ) : 'هنوز کدی ثبت نشده است' ) . '</strong></p>'; }
		echo '</div>';
	}

	private static function is_local(): bool {
		$host = wp_parse_url( home_url(), PHP_URL_HOST );
		return is_string( $host ) && in_array( strtolower( $host ), array( 'localhost', '127.0.0.1', '::1' ), true );
	}
}
