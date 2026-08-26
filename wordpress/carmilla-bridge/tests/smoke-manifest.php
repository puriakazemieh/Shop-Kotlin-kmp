<?php
define( 'ABSPATH', __DIR__ . '/' );
require dirname( __DIR__ ) . '/includes/class-cb-manifest-controller.php';
$features = CB_Manifest_Controller::resolve_features( array( 'academy.quiz' => true, 'academy.core' => false, 'wallet' => true, 'commerce.core' => false ) );
$invalid = CB_Manifest_Controller::dependency_violations( array(
	'content.blog' => false,
	'academy.core' => false,
	'academy.quiz' => true,
) );
$valid = CB_Manifest_Controller::dependency_violations( array(
	'content.blog' => true,
	'academy.core' => true,
	'academy.quiz' => true,
) );
$ok = ! $features['academy.quiz'] && ! $features['wallet'] && $features['content.blog']
	&& count( $invalid ) === 1 && $invalid[0] === 'academy.quiz' && empty( $valid );
echo $ok ? "ALL PASSED\n" : "FAILED\n";
exit( $ok ? 0 : 1 );
