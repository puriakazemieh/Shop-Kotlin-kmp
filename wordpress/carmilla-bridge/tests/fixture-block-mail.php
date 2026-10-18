<?php
/** This must-use plugin belongs only in a disposable synthetic fixture. */
if ( getenv( 'CARMILLA_BROWSER_QA_ONLY' ) !== 'yes' ) { exit; }
add_filter( 'pre_wp_mail', '__return_true' );
