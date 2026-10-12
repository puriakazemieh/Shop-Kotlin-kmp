<?php
/**
 * Theme-only Messaging administration.
 *
 * Bridge owns this screen when installed. This fallback deliberately uses the
 * same Core option schema and sender so a Theme-only installation can configure
 * and send messages without an extra plugin.
 */
if ( ! defined( 'ABSPATH' ) || class_exists( 'CB_Integrations_Controller' ) ) {
	return;
}

add_action( 'admin_menu', 'carmilla_theme_register_messaging_page', 20 );
add_action( 'admin_post_carmilla_theme_test_message', 'carmilla_theme_handle_test_message' );

function carmilla_theme_register_messaging_page() {
	if ( class_exists( 'CB_Integrations_Controller' ) ) {
		return;
	}
	add_submenu_page(
		'carmilla-settings',
		__( 'Carmilla Integrations', 'carmilla' ),
		__( 'Integrations', 'carmilla' ),
		'manage_options',
		'carmilla-integrations',
		'carmilla_theme_render_messaging_page'
	);
}

function carmilla_theme_handle_test_message() {
	if ( ! current_user_can( 'manage_options' ) ) {
		wp_die( esc_html__( 'You do not have sufficient permissions to access this page.', 'carmilla' ) );
	}
	check_admin_referer( 'carmilla_theme_test_message', 'carmilla_theme_test_message_nonce' );

	$type = isset( $_POST['test_type'] ) ? sanitize_key( $_POST['test_type'] ) : '';
	$recipient = isset( $_POST['test_recipient'] ) ? sanitize_text_field( $_POST['test_recipient'] ) : '';
	$body = isset( $_POST['test_body'] ) ? sanitize_textarea_field( $_POST['test_body'] ) : '';
	if ( ! in_array( $type, [ 'sms', 'email' ], true ) || '' === $recipient || '' === $body ) {
		wp_die( esc_html__( 'Recipient and message are required.', 'carmilla' ) );
	}

	if ( 'email' === $type ) {
		$result = \Carmilla\Core\Messaging\MessageSender::send_email( $recipient, __( 'Carmilla test message', 'carmilla' ), $body );
		if ( false === $result ) {
			wp_die( esc_html__( 'Email sending failed.', 'carmilla' ) );
		}
	} else {
		$result = \Carmilla\Core\Messaging\MessageSender::send_sms( $recipient, $body );
		if ( is_wp_error( $result ) ) {
			wp_die( esc_html( $result->get_error_message() ) );
		}
	}

	wp_safe_redirect( add_query_arg( [ 'page' => 'carmilla-integrations', 'tab' => 'test', 'status' => 'success' ], admin_url( 'admin.php' ) ) );
	exit;
}

function carmilla_theme_render_messaging_page() {
	if ( ! current_user_can( 'manage_options' ) ) {
		return;
	}
	$tab = isset( $_GET['tab'] ) ? sanitize_key( $_GET['tab'] ) : 'sms';
	$tabs = [ 'sms' => 'SMS', 'email' => 'Email', 'templates' => 'Templates', 'health' => 'Health', 'test' => 'Test' ];
	if ( ! isset( $tabs[ $tab ] ) ) {
		$tab = 'sms';
	}
	$settings = get_option( \Carmilla\Core\Settings\MessageSettings::OPTION_NAME, [] );
	$value = static function ( $key ) use ( $settings ) { return $settings[ $key ] ?? ''; };
	?>
	<div class="wrap"><h1><?php esc_html_e( 'Carmilla Integrations', 'carmilla' ); ?></h1>
		<h2 class="nav-tab-wrapper"><?php foreach ( $tabs as $key => $label ) : ?>
			<a href="<?php echo esc_url( add_query_arg( [ 'page' => 'carmilla-integrations', 'tab' => $key ], admin_url( 'admin.php' ) ) ); ?>" class="nav-tab <?php echo $tab === $key ? 'nav-tab-active' : ''; ?>"><?php echo esc_html( $label ); ?></a>
		<?php endforeach; ?></h2>
		<?php if ( 'test' === $tab ) : ?>
			<?php if ( isset( $_GET['status'] ) && 'success' === $_GET['status'] ) : ?><div class="notice notice-success"><p><?php esc_html_e( 'Test message sent.', 'carmilla' ); ?></p></div><?php endif; ?>
			<form method="post" action="<?php echo esc_url( admin_url( 'admin-post.php' ) ); ?>"><?php wp_nonce_field( 'carmilla_theme_test_message', 'carmilla_theme_test_message_nonce' ); ?><input type="hidden" name="action" value="carmilla_theme_test_message"><table class="form-table"><tr><th><label for="test_type">Type</label></th><td><select id="test_type" name="test_type"><option value="sms">SMS</option><option value="email">Email</option></select></td></tr><tr><th><label for="test_recipient">Recipient</label></th><td><input class="regular-text" id="test_recipient" name="test_recipient" required></td></tr><tr><th><label for="test_body">Message</label></th><td><textarea class="large-text" rows="3" id="test_body" name="test_body" required></textarea></td></tr></table><?php submit_button( __( 'Send test', 'carmilla' ) ); ?></form>
		<?php elseif ( 'health' === $tab ) : ?>
			<table class="widefat striped"><tbody><tr><td>SMS provider</td><td><?php echo ! empty( $value( 'sms_api_url' ) ) ? esc_html__( 'Ready', 'carmilla' ) : esc_html__( 'Needs configuration', 'carmilla' ); ?></td></tr><tr><td>Email transport</td><td><?php echo function_exists( 'wp_mail' ) ? esc_html__( 'Ready', 'carmilla' ) : esc_html__( 'Unavailable', 'carmilla' ); ?></td></tr></tbody></table>
		<?php else : ?>
			<?php settings_errors( \Carmilla\Core\Settings\MessageSettings::OPTION_NAME ); ?><form method="post" action="options.php"><?php settings_fields( 'carmilla_options' ); ?><table class="form-table">
			<?php if ( 'sms' === $tab ) : ?><tr><th><label for="sms_provider">Provider</label></th><td><input class="regular-text" id="sms_provider" name="carmilla_message_settings[sms_provider]" value="<?php echo esc_attr( $value( 'sms_provider' ) ); ?>"></td></tr><tr><th><label for="sms_api_url">HTTPS API URL</label></th><td><input class="regular-text code" type="url" id="sms_api_url" name="carmilla_message_settings[sms_api_url]" value="<?php echo esc_attr( $value( 'sms_api_url' ) ); ?>"></td></tr><tr><th><label for="sms_api_key">API key</label></th><td><input class="regular-text" type="password" autocomplete="new-password" id="sms_api_key" name="carmilla_message_settings[sms_api_key]"><p class="description">Leave blank to keep the existing key.</p></td></tr><tr><th><label for="sms_success_code">Success code</label></th><td><input class="small-text" type="number" id="sms_success_code" name="carmilla_message_settings[sms_success_code]" value="<?php echo esc_attr( $value( 'sms_success_code' ) ?: '200' ); ?>"></td></tr>
			<?php elseif ( 'email' === $tab ) : ?><tr><th><label for="smtp_host">SMTP host</label></th><td><input class="regular-text" id="smtp_host" name="carmilla_message_settings[smtp_host]" value="<?php echo esc_attr( $value( 'smtp_host' ) ); ?>"></td></tr><tr><th><label for="smtp_port">Port</label></th><td><input class="small-text" type="number" id="smtp_port" name="carmilla_message_settings[smtp_port]" value="<?php echo esc_attr( $value( 'smtp_port' ) ?: '587' ); ?>"></td></tr><tr><th><label for="smtp_user">Username</label></th><td><input class="regular-text" id="smtp_user" name="carmilla_message_settings[smtp_user]" value="<?php echo esc_attr( $value( 'smtp_user' ) ); ?>"></td></tr><tr><th><label for="smtp_pass">Password</label></th><td><input class="regular-text" type="password" autocomplete="new-password" id="smtp_pass" name="carmilla_message_settings[smtp_pass]"><p class="description">Leave blank to keep the existing password.</p></td></tr><tr><th><label for="email_from">From email</label></th><td><input class="regular-text" type="email" id="email_from" name="carmilla_message_settings[email_from]" value="<?php echo esc_attr( $value( 'email_from' ) ); ?>"></td></tr>
			<?php else : ?><tr><th><label for="sms_template">SMS template</label></th><td><textarea class="large-text" rows="4" id="sms_template" name="carmilla_message_settings[sms_template]"><?php echo esc_textarea( $value( 'sms_template' ) ); ?></textarea></td></tr><tr><th><label for="email_template">Email template</label></th><td><textarea class="large-text" rows="6" id="email_template" name="carmilla_message_settings[email_template]"><?php echo esc_textarea( $value( 'email_template' ) ); ?></textarea></td></tr><?php endif; ?>
			</table><?php submit_button( __( 'Save settings', 'carmilla' ) ); ?></form>
		<?php endif; ?>
	</div><?php
}
