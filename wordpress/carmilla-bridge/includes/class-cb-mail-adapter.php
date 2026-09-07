<?php
if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class CB_Mail_Adapter {

    public static function init() {
        add_action( 'phpmailer_init', [__CLASS__, 'configure_smtp'] );
    }

    public static function configure_smtp( \ ) {
        \ = get_option('carmilla_message_settings', []);
        
        \ = \['smtp_host'] ?? '';
        if ( empty(\) ) {
            return; // Not configured
        }

        \->isSMTP();
        \->Host       = \;
        \->SMTPAuth   = !empty(\['smtp_user']);
        \->Port       = intval(\['smtp_port'] ?? 587);
        \->Username   = \['smtp_user'] ?? '';
        \->Password   = \['smtp_pass'] ?? '';
        \->SMTPSecure = \['smtp_secure'] ?? 'tls';
        
        \ = \['email_from'] ?? '';
        if ( !empty(\) ) {
            \->From = \;
            \->FromName = \['email_from_name'] ?? get_bloginfo('name');
        }
    }

    public static function send(\, \, \, \ = '', \ = array()) {
        // Fallback to default wp_mail so standard SMTP plugins work.
        return wp_mail(\, \, \, \, \);
    }
}
