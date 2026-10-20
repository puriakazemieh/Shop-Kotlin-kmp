<?php
/** Transactional deletion for Carmilla-managed WordPress accounts. */
if ( ! defined( 'ABSPATH' ) ) { exit; }

final class CB_Managed_User_Deletion {
	private static $instance;

	public static function boot(): void {
		if ( self::$instance ) { return; }
		self::$instance = new self();
		add_filter( 'map_meta_cap', array( self::$instance, 'block_native_delete' ), 20, 4 );
		add_action( 'admin_menu', array( self::$instance, 'register_page' ), 30 );
		add_action( 'admin_post_cb_managed_user_delete', array( self::$instance, 'handle_delete' ) );
	}

	public function block_native_delete( $caps, $cap, $user_id, $args ) {
		if ( $cap !== 'delete_user' || empty( $args[0] ) || ! $this->is_managed( (int) $args[0] ) ) { return $caps; }
		return array( 'do_not_allow' );
	}

	public function register_page(): void {
		add_submenu_page( 'carmilla-settings', 'حذف امن حساب', 'حذف امن حساب', 'manage_options', 'carmilla-managed-user-delete', array( $this, 'render_page' ) );
	}

	public function render_page(): void {
		if ( ! current_user_can( 'manage_options' ) ) { wp_die( esc_html__( 'دسترسی ندارید.', 'carmilla-bridge' ) ); }
		$status = isset( $_GET['cb_delete_status'] ) ? sanitize_key( (string) $_GET['cb_delete_status'] ) : '';
		echo '<div class="wrap"><h1>حذف امن حساب کارمیلا</h1>';
		if ( $status ) { echo '<div class="notice notice-' . ( $status === 'deleted' ? 'success' : 'error' ) . '"><p>' . esc_html( $this->message( $status ) ) . '</p></div>'; }
		echo '<p>این صفحه فقط حساب‌های دارای هویت کارمیلا را حذف می‌کند. حساب دارای محتوا، سفارش یا نظر وابسته حذف نمی‌شود.</p>';
		echo '<form method="post" action="' . esc_url( admin_url( 'admin-post.php' ) ) . '">';
		wp_nonce_field( 'cb_managed_user_delete' );
		echo '<input type="hidden" name="action" value="cb_managed_user_delete"><p><label>شناسه عددی کاربر <input name="account_id" type="number" min="1" required></label></p>';
		echo '<p><label>برای تأیید، عبارت حذف را وارد کنید <input name="confirmation" type="text" required></label></p><p><button class="button button-primary">حذف امن حساب</button></p></form></div>';
	}

	public function handle_delete(): void {
		if ( ! current_user_can( 'manage_options' ) ) { wp_die( esc_html__( 'دسترسی ندارید.', 'carmilla-bridge' ) ); }
		check_admin_referer( 'cb_managed_user_delete' );
		$id = isset( $_POST['account_id'] ) ? absint( $_POST['account_id'] ) : 0;
		$confirmed = isset( $_POST['confirmation'] ) && hash_equals( 'حذف', trim( (string) wp_unslash( $_POST['confirmation'] ) ) );
		$status = $confirmed ? $this->delete( $id ) : 'confirmation_required';
		wp_safe_redirect( add_query_arg( 'cb_delete_status', $status, admin_url( 'admin.php?page=carmilla-managed-user-delete' ) ) );
		exit;
	}

	private function delete( int $id ): string {
		global $wpdb;
		if ( $id < 1 || $id === get_current_user_id() ) { return 'invalid_account'; }
		if ( ! $this->is_managed( $id ) ) { return 'not_managed'; }
		if ( $this->has_dependencies( $id ) ) { return 'dependencies_exist'; }
		if ( $wpdb->query( 'START TRANSACTION' ) === false ) { return 'unavailable'; }
		try {
			$meta = $wpdb->delete( $wpdb->usermeta, array( 'user_id' => $id ), array( '%d' ) );
			$deleted = $meta !== false && $wpdb->delete( $wpdb->users, array( 'ID' => $id ), array( '%d' ) ) === 1;
			$remaining = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE ID=%d", $id ) );
			if ( ! $deleted || $remaining !== 0 ) { $wpdb->query( 'ROLLBACK' ); return 'unavailable'; }
			if ( $wpdb->query( 'COMMIT' ) === false ) { return 'unavailable'; }
			clean_user_cache( $id );
			do_action( 'carmilla_managed_user_deleted', $id );
			return 'deleted';
		} catch ( Throwable $error ) { $wpdb->query( 'ROLLBACK' ); return 'unavailable'; }
	}

	private function is_managed( int $id ): bool {
		return $id > 0 && ( metadata_exists( 'user', $id, 'cb_mobile' ) || metadata_exists( 'user', $id, 'cb_identity_managed' ) );
	}

	private function has_dependencies( int $id ): bool {
		global $wpdb;
		$post_count = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->posts} WHERE post_author=%d", $id ) );
		$comment_count = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->comments} WHERE user_id=%d", $id ) );
		return $post_count > 0 || $comment_count > 0;
	}

	private function message( string $status ): string {
		$messages = array( 'deleted' => 'حساب با موفقیت حذف شد.', 'confirmation_required' => 'عبارت تأیید درست نیست.', 'dependencies_exist' => 'این حساب دادهٔ وابسته دارد و برای جلوگیری از حذف ناقص حذف نشد.', 'not_managed' => 'این حساب، حساب مدیریت‌شدهٔ کارمیلا نیست.', 'invalid_account' => 'حذف این حساب مجاز نیست.', 'unavailable' => 'حذف انجام نشد؛ هیچ نتیجهٔ موفقی ثبت نشده است.' );
		return $messages[ $status ] ?? 'درخواست نامعتبر است.';
	}
}
