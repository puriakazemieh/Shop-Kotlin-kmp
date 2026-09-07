<?php
namespace Carmilla\Core\Payment;

/**
 * Wallet Service for Carmilla Core.
 * Manages user balance and internal transactions.
 */
class WalletService {
    
    private const WALLET_META_KEY = '_carmilla_wallet_balance';
    
    public static function get_balance($user_id) {
        return (float) get_user_meta($user_id, self::WALLET_META_KEY, true);
    }
    
    public static function add_funds($user_id, $amount, $reason = '') {
        $current = self::get_balance($user_id);
        $new_balance = $current + $amount;
        update_user_meta($user_id, self::WALLET_META_KEY, $new_balance);
        self::log_transaction($user_id, $amount, $new_balance, 'credit', $reason);
        return $new_balance;
    }
    
    public static function deduct_funds($user_id, $amount, $reason = '') {
        $current = self::get_balance($user_id);
        if ($current < $amount) {
            return false; // Insufficient funds
        }
        $new_balance = $current - $amount;
        update_user_meta($user_id, self::WALLET_META_KEY, $new_balance);
        self::log_transaction($user_id, -$amount, $new_balance, 'debit', $reason);
        return $new_balance;
    }

    private static function log_transaction($user_id, $amount, $balance, $type, $reason) {
        global $wpdb;
        $table = $wpdb->prefix . 'carmilla_wallet_logs';
        
        // Ensure table exists (in a real scenario, this is done in SchemaRunner)
        $wpdb->insert($table, [
            'user_id' => $user_id,
            'amount' => $amount,
            'balance_after' => $balance,
            'type' => $type,
            'reason' => $reason,
            'created_at' => current_time('mysql')
        ]);
    }
}
