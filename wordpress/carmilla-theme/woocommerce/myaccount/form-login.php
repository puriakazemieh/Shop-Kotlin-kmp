<?php
/**
 * Login / register ← AuthScreen — the reference AUTH card wrapped around
 * WooCommerce's real login + register forms (username/password on the web; the
 * app uses phone/OTP via the plugin REST). Field names stay WooCommerce's so
 * authentication keeps working. Mirrors docs/design-reference/*.html.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

do_action( 'woocommerce_before_customer_login_form' );

$brand_init = mb_substr( wp_strip_all_tags( get_bloginfo( 'name' ) ), 0, 1, 'UTF-8' );
$can_register = 'yes' === get_option( 'woocommerce_enable_myaccount_registration' );
?>
<div class="cb-auth" style="animation:fadeUp .35s both;min-height:70vh;display:flex;align-items:flex-start;justify-content:center;padding-top:24px;">
	<div style="width:100%;max-width:400px;">
		<div style="text-align:center;margin-bottom:26px;">
			<div style="width:64px;height:64px;border-radius:19px;background:var(--accent);display:grid;place-items:center;color:#fff;font-weight:800;font-size:32px;margin:0 auto 16px;"><?php echo esc_html( $brand_init ); ?></div>
			<h1 style="font-size:24px;font-weight:800;margin:0 0 8px;">ورود به <?php bloginfo( 'name' ); ?></h1>
			<p style="font-size:13px;color:var(--ink-soft);margin:0;line-height:1.8;">برای ادامه وارد شوید یا حساب جدید بسازید</p>
		</div>

		<div style="background:var(--surface);border:1px solid var(--line);border-radius:22px;padding:24px;">
			<form class="woocommerce-form woocommerce-form-login login" method="post">
				<?php do_action( 'woocommerce_login_form_start' ); ?>
				<div style="font-size:13px;font-weight:700;margin-bottom:8px;">نام کاربری یا ایمیل</div>
				<input type="text" class="woocommerce-Input input-text" name="username" autocomplete="username" value="<?php echo ( ! empty( $_POST['username'] ) ) ? esc_attr( wp_unslash( $_POST['username'] ) ) : ''; // phpcs:ignore ?>" style="width:100%;box-sizing:border-box;border:1.5px solid var(--line);border-radius:13px;padding:13px 15px;font-family:inherit;font-size:14px;background:var(--surface-2);color:var(--ink);outline:none;margin-bottom:14px;">

				<div style="font-size:13px;font-weight:700;margin-bottom:8px;">گذرواژه</div>
				<input type="password" class="woocommerce-Input input-text" name="password" autocomplete="current-password" style="width:100%;box-sizing:border-box;border:1.5px solid var(--line);border-radius:13px;padding:13px 15px;font-family:inherit;font-size:14px;background:var(--surface-2);color:var(--ink);outline:none;margin-bottom:14px;">

				<?php do_action( 'woocommerce_login_form' ); ?>

				<label style="display:flex;align-items:center;gap:8px;font-size:12px;color:var(--ink-soft);margin-bottom:16px;cursor:pointer;">
					<input type="checkbox" name="rememberme" value="forever" style="accent-color:var(--accent);"> مرا به خاطر بسپار
				</label>

				<?php wp_nonce_field( 'woocommerce-login', 'woocommerce-login-nonce' ); ?>
				<button type="submit" class="woocommerce-button button woocommerce-form-login__submit" name="login" value="ورود" style="width:100%;background:var(--accent);color:#fff;font-weight:700;font-size:15px;padding:15px;border-radius:14px;border:none;cursor:pointer;font-family:inherit;">ورود به حساب</button>

				<p style="text-align:center;margin:14px 0 0;font-size:12px;"><a href="<?php echo esc_url( wp_lostpassword_url() ); ?>" style="color:var(--accent);">گذرواژه‌ام را فراموش کرده‌ام</a></p>
				<?php do_action( 'woocommerce_login_form_end' ); ?>
			</form>

			<?php if ( $can_register ) : ?>
				<div style="display:flex;align-items:center;gap:12px;margin:20px 0;"><div style="flex:1;height:1px;background:var(--line);"></div><span style="font-size:11.5px;color:var(--ink-soft);">یا ثبت‌نام</span><div style="flex:1;height:1px;background:var(--line);"></div></div>
				<form method="post" class="woocommerce-form woocommerce-form-register register">
					<?php do_action( 'woocommerce_register_form_start' ); ?>
					<?php if ( 'no' === get_option( 'woocommerce_registration_generate_username' ) ) : ?>
						<div style="font-size:13px;font-weight:700;margin-bottom:8px;">نام کاربری</div>
						<input type="text" class="woocommerce-Input input-text" name="username" autocomplete="username" value="<?php echo ( ! empty( $_POST['username'] ) ) ? esc_attr( wp_unslash( $_POST['username'] ) ) : ''; // phpcs:ignore ?>" style="width:100%;box-sizing:border-box;border:1.5px solid var(--line);border-radius:13px;padding:13px 15px;font-family:inherit;font-size:14px;background:var(--surface-2);color:var(--ink);outline:none;margin-bottom:14px;">
					<?php endif; ?>
					<div style="font-size:13px;font-weight:700;margin-bottom:8px;">ایمیل</div>
					<input type="email" class="woocommerce-Input input-text" name="email" autocomplete="email" value="<?php echo ( ! empty( $_POST['email'] ) ) ? esc_attr( wp_unslash( $_POST['email'] ) ) : ''; // phpcs:ignore ?>" style="width:100%;box-sizing:border-box;border:1.5px solid var(--line);border-radius:13px;padding:13px 15px;font-family:inherit;font-size:14px;background:var(--surface-2);color:var(--ink);outline:none;margin-bottom:14px;">
					<?php if ( 'no' === get_option( 'woocommerce_registration_generate_password' ) ) : ?>
						<div style="font-size:13px;font-weight:700;margin-bottom:8px;">گذرواژه</div>
						<input type="password" class="woocommerce-Input input-text" name="password" autocomplete="new-password" style="width:100%;box-sizing:border-box;border:1.5px solid var(--line);border-radius:13px;padding:13px 15px;font-family:inherit;font-size:14px;background:var(--surface-2);color:var(--ink);outline:none;margin-bottom:14px;">
					<?php endif; ?>
					<?php do_action( 'woocommerce_register_form' ); ?>
					<?php wp_nonce_field( 'woocommerce-register', 'woocommerce-register-nonce' ); ?>
					<button type="submit" class="woocommerce-Button button" name="register" value="ثبت‌نام" style="width:100%;background:var(--accent-soft);color:var(--accent);font-weight:700;font-size:15px;padding:15px;border-radius:14px;border:none;cursor:pointer;font-family:inherit;">ساخت حساب جدید</button>
					<?php do_action( 'woocommerce_register_form_end' ); ?>
				</form>
			<?php endif; ?>

			<div style="font-size:11px;color:var(--ink-soft);text-align:center;margin-top:16px;line-height:1.8;">ورود شما به معنای پذیرش <span style="color:var(--accent);">قوانین و حریم خصوصی</span> است.</div>
		</div>
	</div>
</div>
<?php
do_action( 'woocommerce_after_customer_login_form' );
