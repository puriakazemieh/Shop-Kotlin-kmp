<?php
namespace Carmilla\Core\Payment;

if (!class_exists('WC_Payment_Gateway')) {
    return;
}

class WC_Gateway_Carmilla_Wallet extends \WC_Payment_Gateway {

    public function __construct() {
        $this->id                 = 'carmilla_wallet';
        $this->icon               = '';
        $this->has_fields         = false;
        $this->method_title       = 'کیف پول کارمیلا (Carmilla Wallet)';
        $this->method_description = 'پرداخت امن و سریع از طریق موجودی کیف پول کاربر.';
        
        $this->init_form_fields();
        $this->init_settings();
        
        $this->title       = $this->get_option('title');
        $this->description = $this->get_option('description');
        
        add_action('woocommerce_update_options_payment_gateways_' . $this->id, [$this, 'process_admin_options']);
    }

    public function init_form_fields() {
        $this->form_fields = [
            'enabled' => [
                'title'   => 'فعال/غیرفعال',
                'type'    => 'checkbox',
                'label'   => 'فعال‌سازی درگاه کیف پول',
                'default' => 'yes'
            ],
            'title' => [
                'title'       => 'عنوان',
                'type'        => 'text',
                'description' => 'عنوانی که کاربر در زمان پرداخت می‌بیند.',
                'default'     => 'پرداخت با کیف پول',
                'desc_tip'    => true,
            ],
            'description' => [
                'title'       => 'توضیحات',
                'type'        => 'textarea',
                'description' => 'توضیحاتی که به کاربر نشان داده می‌شود.',
                'default'     => 'کسر مبلغ از موجودی کیف پول شما.',
            ]
        ];
    }

    public function process_payment($order_id) {
        $order = wc_get_order($order_id);
        $user_id = $order->get_customer_id();
        $total = $order->get_total();

        if (!$user_id) {
            wc_add_notice('برای استفاده از کیف پول باید وارد حساب کاربری خود شوید.', 'error');
            return;
        }

        $balance = WalletService::get_balance($user_id);
        
        if ($balance < $total) {
            wc_add_notice('موجودی کیف پول شما کافی نیست.', 'error');
            return;
        }

        // Deduct funds
        $success = WalletService::deduct_funds($user_id, $total, 'پرداخت سفارش #' . $order_id);

        if ($success !== false) {
            $order->payment_complete();
            $order->add_order_note('پرداخت موفق از طریق کیف پول. مبلغ کسر شده: ' . $total);
            wc_reduce_stock_levels($order_id);
            WC()->cart->empty_cart();

            return [
                'result'   => 'success',
                'redirect' => $this->get_return_url($order)
            ];
        } else {
            wc_add_notice('خطا در کسر موجودی.', 'error');
            return;
        }
    }
}
