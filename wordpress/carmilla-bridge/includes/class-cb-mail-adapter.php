<?php
if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class CB_Mail_Adapter {
    public static function send(\, \, \, \ = '', \ = array()) {
        // Fallback to default wp_mail so standard SMTP plugins work.
        return wp_mail(\, \, \, \, \);
    }
}
