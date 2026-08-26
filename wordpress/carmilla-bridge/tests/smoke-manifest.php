<?php
define( 'ABSPATH', __DIR__ . '/' );
require dirname( __DIR__ ) . '/includes/class-cb-manifest-controller.php';
$features = CB_Manifest_Controller::resolve_features( array( 'academy.quiz' => true, 'academy.core' => false, 'wallet' => true, 'commerce.core' => false ) );
$ok = ! $features['academy.quiz'] && ! $features['wallet'] && $features['content.blog'];
echo $ok ? "ALL PASSED\n" : "FAILED\n";
exit( $ok ? 0 : 1 );
