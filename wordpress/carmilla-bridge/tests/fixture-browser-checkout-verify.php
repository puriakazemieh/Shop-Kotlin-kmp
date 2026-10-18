<?php
if ( getenv( 'CARMILLA_BROWSER_QA_ONLY' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) getenv( 'CARMILLA_REFERENCE_PREFIX' ) ) ) { fwrite( STDERR, "Disposable fixture required.\n" ); exit( 2 ); }
$orders = wc_get_orders( array( 'limit' => 3, 'orderby' => 'date', 'order' => 'DESC' ) );
if ( count( $orders ) !== 1 ) { fwrite( STDERR, "Unexpected synthetic order count.\n" ); exit( 1 ); }
$order = $orders[0]; $items = $order->get_items(); $item = reset( $items );
if ( $order->get_payment_method() !== 'cod' || $order->get_status() !== 'processing' || count( $items ) !== 1 || ! $item || $item->get_name() !== 'Synthetic QA Product' || (int) $item->get_quantity() !== 1 || (float) $order->get_total() !== 100.0 || $order->get_billing_email() !== 'checkout@example.test' ) { fwrite( STDERR, "Synthetic order mismatch.\n" ); exit( 1 ); }
echo "PASS synthetic offline order persisted with expected item and total\n";
