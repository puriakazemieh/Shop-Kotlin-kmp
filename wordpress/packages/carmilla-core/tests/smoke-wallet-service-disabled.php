<?php
/** Synthetic wallet mutation and direct gateway guard; no real WooCommerce checkout. */
$writes = 0;
$notices = array();
$stock_reductions = 0;
$order_paid = false;
$order_notes = 0;
$cart_empties = 0;
function get_user_meta( $user_id, $key, $single ) { return '250'; }
function update_user_meta( $user_id, $key, $value ) { ++$GLOBALS['writes']; return true; }
class Fake_Wallet_DB {
    public $prefix = 'wp_';
    public $inserts = 0;
    public function insert( $table, $data ) { ++$this->inserts; return 1; }
}
$wpdb = new Fake_Wallet_DB();
class WC_Payment_Gateway {}
class Fake_Wallet_Order {
    public function get_customer_id() { return 17; }
    public function get_total() { return 50; }
    public function payment_complete() { $GLOBALS['order_paid'] = true; }
    public function add_order_note( $note ) { ++$GLOBALS['order_notes']; }
}
function wc_get_order( $order_id ) { return new Fake_Wallet_Order(); }
function wc_add_notice( $message, $type ) { $GLOBALS['notices'][] = $type; }
function wc_reduce_stock_levels( $order_id ) { ++$GLOBALS['stock_reductions']; }
function WC() { return (object) array( 'cart' => new class {
    public function empty_cart() { ++$GLOBALS['cart_empties']; }
} ); }
require dirname( __DIR__ ) . '/inc/Payment/WalletService.php';
require dirname( __DIR__ ) . '/inc/Payment/WC_Gateway_Carmilla_Wallet.php';
use Carmilla\Core\Payment\WalletService;
use Carmilla\Core\Payment\WC_Gateway_Carmilla_Wallet;

$before = WalletService::get_balance( 17 );
$credit = WalletService::add_funds( 17, 25, 'synthetic credit' );
$debit = WalletService::deduct_funds( 17, 50, 'synthetic debit' );
$gateway = ( new ReflectionClass( WC_Gateway_Carmilla_Wallet::class ) )->newInstanceWithoutConstructor();
$payment = $gateway->process_payment( 123 );
$after = WalletService::get_balance( 17 );
$ok = $before === 250.0 && $after === 250.0 && $credit === false && $debit === false &&
    $writes === 0 && $wpdb->inserts === 0 && $payment === null && ! $order_paid &&
    $order_notes === 0 && $stock_reductions === 0 && $cart_empties === 0 && $notices === array( 'error' );
echo $ok ? "PASS wallet credit/debit and direct gateway fail closed\n" : "FAIL wallet mutation guard\n";
exit( $ok ? 0 : 1 );
