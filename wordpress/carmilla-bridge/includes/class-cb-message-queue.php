<?php
if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class CB_Message_Queue {

    public static function init() {
        add_action('cb_process_sms_queue', [__CLASS__, 'process_sms'], 10, 2);
        add_action('cb_process_email_queue', [__CLASS__, 'process_email'], 10, 3);
    }

    /**
     * Queues an SMS.
     */
    public static function queue_sms($to, $message) {
        if (!function_exists('as_enqueue_async_action')) {
            return CB_SMS_HTTP_Adapter::send($to, $message);
        }

        $args = ['to' => $to, 'message' => $message];
        // dedupe
        if (as_has_scheduled_action('cb_process_sms_queue', $args)) {
            return new WP_Error('duplicate', 'SMS already queued');
        }

        return as_enqueue_async_action('cb_process_sms_queue', $args);
    }

    /**
     * Queues an Email.
     */
    public static function queue_email($to, $subject, $message) {
        if (!function_exists('as_enqueue_async_action')) {
            return CB_Mail_Adapter::send($to, $subject, $message);
        }

        $args = ['to' => $to, 'subject' => $subject, 'message' => $message];
        // dedupe
        if (as_has_scheduled_action('cb_process_email_queue', $args)) {
            return new WP_Error('duplicate', 'Email already queued');
        }

        return as_enqueue_async_action('cb_process_email_queue', $args);
    }

    public static function process_sms($to, $message) {
        $result = CB_SMS_HTTP_Adapter::send($to, $message);
        if (is_wp_error($result)) {
            $code = $result->get_error_code();
            // non-retryable errors
            if ($code === 'invalid_config' || $code === 'missing_config' || strpos($code, '400') !== false) {
                error_log('CB SMS Non-retryable error: ' . $result->get_error_message());
                return; // don't throw, so it doesn't retry
            }
            throw new Exception('SMS sending failed: ' . $result->get_error_message()); // throw to retry
        }
    }

    public static function process_email($to, $subject, $message) {
        $result = CB_Mail_Adapter::send($to, $subject, $message);
        if ($result === false) {
            // wp_mail returns false on failure, but we don't have detailed error codes unless phpmailer_init catches it
            throw new Exception('Email sending failed.');
        }
    }
}
