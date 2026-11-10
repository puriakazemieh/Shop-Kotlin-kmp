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
        // Disabled until balance, ledger and order effects are atomic and idempotent.
        return false;
    }
    
    public static function deduct_funds($user_id, $amount, $reason = '') {
        // Never debit a balance before an unchecked ledger write.
        return false;
    }
}
