<?php
/** Inactive administrator notice; caller must explicitly invoke it. */
if ( ! defined( 'ABSPATH' ) ) { exit; }
require_once __DIR__ . '/class-cb-deletion-incident-store.php';

final class CB_Deletion_Incident_Notice {
	private $store;
	public function __construct( CB_Deletion_Incident_Store $store ) { $this->store = $store; }

	/** Display bounded observations, never a claim that deletion was recovered. */
	public function render(): void {
		if ( ! current_user_can( 'manage_options' ) ) { return; }
		$list = $this->store->list_unexpired( 100 );
		if ( $list['status'] === 'FORBIDDEN' ) { return; }
		if ( $list['status'] !== 'AVAILABLE' ) {
			echo '<div class="notice notice-error"><p>' . esc_html__( 'بررسی گزارش حذف کاربران در دسترس نیست؛ وضعیت حذف را جداگانه بررسی کنید.', 'carmilla-bridge' ) . '</p></div>';
			return;
		}
		if ( ! $list['incidents'] ) { return; }
		echo '<div class="notice notice-warning"><p>' . esc_html__( 'ممکن است حذف برخی کاربران کامل نشده باشد. هیچ بازیابی خودکاری انجام نشده است؛ وضعیت را پیش از اقدام بررسی کنید.', 'carmilla-bridge' ) . '</p>';
		echo '<p>' . esc_html__( 'حداکثر ۱۰۰ گزارش اخیر نمایش داده می‌شود؛ نبود گزارش نیز سلامت همه حذف‌ها را ثابت نمی‌کند.', 'carmilla-bridge' ) . '</p><ul>';
		foreach ( $list['incidents'] as $incident ) {
			$id = (int) $incident['account_id'];
			$code = $incident['code'] === 'PARTIAL_USER_SURVIVES'
				? esc_html__( 'حساب باقی مانده و بخشی از اطلاعات تغییر کرده است.', 'carmilla-bridge' )
				: esc_html__( 'حساب همچنان وجود دارد.', 'carmilla-bridge' );
			echo '<li>' . esc_html__( 'شناسه حساب:', 'carmilla-bridge' ) . ' ' . esc_html( (string) $id ) . ' — ' . $code . '</li>';
		}
		echo '</ul></div>';
	}
}
