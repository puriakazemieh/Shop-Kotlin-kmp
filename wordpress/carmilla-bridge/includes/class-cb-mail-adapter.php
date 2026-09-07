<?php
if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class CB_Mail_Adapter {

    public static function init() {
        add_action( 'phpmailer_init', [__CLASS__, 'configure_smtp'] );
    }

    public static function configure_smtp( $phpmailer ) {
        $settings = get_option('carmilla_message_settings', []);
        
        $smtp_host = $settings['smtp_host'] ?? '';
        if ( empty($smtp_host) ) {
            return; // Not configured
        }

        $encrypted_pass = $settings['smtp_pass'] ?? '';
        $smtp_pass = \Carmilla\Core\Settings\MessageSettings::decrypt_secret($encrypted_pass);

        $phpmailer->isSMTP();
        $phpmailer->Host       = $smtp_host;
        $phpmailer->SMTPAuth   = !empty($settings['smtp_user']);
        $phpmailer->Port       = intval($settings['smtp_port'] ?? 587);
        $phpmailer->Username   = $settings['smtp_user'] ?? '';
        $phpmailer->Password   = $smtp_pass;
        $phpmailer->SMTPSecure = $settings['smtp_secure'] ?? 'tls';
        
        $from_email = $settings['email_from'] ?? '';
        if ( !empty($from_email) ) {
            $phpmailer->From = $from_email;
            $phpmailer->FromName = $settings['email_from_name'] ?? get_bloginfo('name');
        }
    }

    public static function send($to, $subject, $message, $headers = '', $attachments = array()) {
        // Fallback to default wp_mail so standard SMTP plugins work.
        return wp_mail($to, $subject, $message, $headers, $attachments);
    }
}
