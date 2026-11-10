<?php
/** Disposable WordPress HTTP fixture. Copy to mu-plugins only for the guarded QA run. */
add_action( 'admin_post_carmilla_qa_wallet_guard', static function (): void {
    if ( ! current_user_can( 'manage_options' ) ) {
        wp_die( 'Forbidden', '', array( 'response' => 403 ) );
    }
    check_admin_referer( 'carmilla_qa_wallet_guard', 'qa_nonce' );
    require_once WP_PLUGIN_DIR . '/carmilla-bridge/packages/carmilla-core/inc/Payment/WalletService.php';
    $user_id = get_current_user_id();
    $service = \Carmilla\Core\Payment\WalletService::class;
    $before = $service::get_balance( $user_id );
    $credit = $service::add_funds( $user_id, 25, 'synthetic HTTP credit' );
    $debit = $service::deduct_funds( $user_id, 50, 'synthetic HTTP debit' );
    $after = $service::get_balance( $user_id );
    wp_send_json( array( 'before' => $before, 'after' => $after, 'credit' => $credit, 'debit' => $debit ) );
} );
