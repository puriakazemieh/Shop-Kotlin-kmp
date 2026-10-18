<?php
/** Only this disposable fixture attaches an inactive observer to native WP hooks. */
require __DIR__ . '/experiment-identity-shared-references.php';
require_once '/bridge/includes/class-cb-deletion-incident-observer.php';

try {
	global $wpdb, $prefix, $wp_version;
	$store_db = new mysqli( '127.0.0.1', 'synthetic_qa', 'synthetic_qa_password', 'carmilla_login_qa', 3306 );
	$observer_db = new mysqli( '127.0.0.1', 'synthetic_qa', 'synthetic_qa_password', 'carmilla_login_qa', 3306 );
	shared_check( $store_db->query( CB_Deletion_Incident_Store::schema_sql( $prefix ) ) === true, 'create disposable incident schema' );
	$store = new CB_Deletion_Incident_Store( $store_db, $prefix, hash( 'sha256', 'synthetic-site' ) );
	$observer = new CB_Deletion_Incident_Observer( $observer_db, $wpdb->users, $wpdb->usermeta, $store, 300 );
	$begin = array(); $results = array(); $listener = array();
	add_action( 'delete_user', static function ( $id ) use ( $observer, &$begin ) { $begin[ $id ] = $observer->begin( (int) $id ); }, 10, 1 );
	add_action( 'deleted_user', static function ( $id ) use ( $observer, &$results ) { $results[ $id ] = $observer->after( (int) $id ); }, 50, 1 );
	add_action( 'deleted_user', static function ( $id ) use ( &$listener ) { $listener[ $id ] = true; }, 99, 1 );
	$trigger = "CREATE TRIGGER `{$prefix}incident_observer_fault` BEFORE DELETE ON {$wpdb->usermeta} FOR EACH ROW BEGIN IF @incident_observer_all=1 OR @incident_observer_mid=OLD.umeta_id THEN SIGNAL SQLSTATE '45000'; END IF; END";
	shared_check( $wpdb->query( $trigger ) !== false, 'create session-specific disposable delete fault' );
	$admin = get_user_by( 'login', 'qa_admin' ); if ( ! $admin instanceof WP_User ) { throw new RuntimeException(); } wp_set_current_user( $admin->ID );
	$normal = shared_user( 'incident_normal', 'normal@example.test' ); add_user_meta( $normal, 'cb_mobile', '09128881001' );
	shared_check( wp_delete_user( $normal ) === true && ( $begin[ $normal ] ?? false ) && ( $results[ $normal ] ?? array() ) === array( 'status' => 'NO_SURVIVING_USER_OBSERVED' ) && ! empty( $listener[ $normal ] ), 'native deletion reaches observer and separate listener without a false surviving-user incident' );
	$partial = shared_user( 'incident_partial', 'partial@example.test' ); $mid = add_user_meta( $partial, 'cb_mobile', '09128881002' );
	$wpdb->query( 'SET @incident_observer_mid=' . (int) $mid ); $native = wp_delete_user( $partial ); $wpdb->query( 'SET @incident_observer_mid=NULL' );
	$partial_result = $results[ $partial ] ?? array();
	shared_check( $native === true && ( $begin[ $partial ] ?? false ) && ( $partial_result['status'] ?? '' ) === 'RECORDED' && ! empty( $listener[ $partial ] ), 'injected partial deletion preserves native return and listener while recording incident' );
	$record = $store->read( $partial_result['incident_id'] ?? '' );
	shared_check( $record === array( 'status' => 'AVAILABLE', 'account_id' => $partial, 'code' => 'PARTIAL_USER_SURVIVES' ) && shared_meta_count( $partial, 'cb_mobile' ) === 1, 'surviving changed user correctly classified and linked' );
	$unchanged = shared_user( 'incident_unchanged', 'unchanged@example.test' ); add_user_meta( $unchanged, 'cb_mobile', '09128881003' );
	$wpdb->query( 'SET @incident_observer_all=1' ); $native = wp_delete_user( $unchanged ); $wpdb->query( 'SET @incident_observer_all=NULL' );
	$unchanged_result = $results[ $unchanged ] ?? array();
	shared_check( $native === true && ( $unchanged_result['status'] ?? '' ) === 'RECORDED' && ! empty( $listener[ $unchanged ] ), 'injected unchanged-survivor incident preserves native behavior' );
	shared_check( $store->read( $unchanged_result['incident_id'] ?? '' ) === array( 'status' => 'AVAILABLE', 'account_id' => $unchanged, 'code' => 'USER_SURVIVES' ), 'unchanged surviving user is not misclassified as partial' );
	shared_check( $observer->after( $partial ) === array( 'status' => 'UNVERIFIED' ), 'missing or repeated begin event is unverified' );
	$repeat = shared_user( 'incident_repeat', 'repeat@example.test' );
	shared_check( $observer->begin( $repeat ) && ! $observer->begin( $repeat ) && $observer->after( $repeat ) === array( 'status' => 'UNVERIFIED' ), 'duplicate begin invalidates ambiguous snapshot' );
	$direct = shared_user( 'incident_direct', 'direct@example.test' );
	shared_check( $wpdb->delete( $wpdb->users, array( 'ID' => $direct ) ) === 1 && ! isset( $begin[ $direct ] ) && ! isset( $results[ $direct ] ), 'direct SQL bypasses the native observer hooks' );
	$broken_db = new mysqli( '127.0.0.1', 'synthetic_qa', 'synthetic_qa_password', 'carmilla_login_qa', 3306 );
	$broken_observer = new CB_Deletion_Incident_Observer( $broken_db, $wpdb->users, $wpdb->usermeta, $store, 300 ); $broken_db->close();
	shared_check( ! $broken_observer->begin( $partial ) && $broken_observer->after( $partial ) === array( 'status' => 'UNVERIFIED' ), 'query failure cannot be interpreted as successful observation' );
	$write_target = shared_user( 'incident_write_failure', 'writefailure@example.test' ); shared_check( $observer->begin( $write_target ), 'capture disposable pre-delete digest before storage failure' );
	$store_db->close(); shared_check( $observer->after( $write_target ) === array( 'status' => 'UNVERIFIED' ), 'incident write failure is not reported as recorded' );
	echo 'PASS inactive native deletion incident observer WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n";
} catch ( Throwable $e ) { fwrite( STDERR, 'FAIL observer fixture: ' . get_class( $e ) . "\n" ); exit( 1 ); }
