<?php
/** Disposable synthetic database only; setup grants CREATE solely on the first table. */
$prefix = getenv( 'CARMILLA_ADAPTER_TEST_PREFIX' );
if ( getenv( 'CARMILLA_ADAPTER_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) { fwrite( STDERR, "Disposable fixture required.\n" ); exit( 2 ); }
define( 'ABSPATH', '/usr/src/wordpress/' ); require_once '/work/includes/class-cb-web-session-migration.php';
try {
	$db = new mysqli( '127.0.0.1', 'synthetic_fault', 'synthetic_fault_password', 'carmilla_adapter_qa', 3306 );
	$migration = new CB_Web_Session_Migration( $db, $prefix ); $failed = false;
	try { $migration->ensure_v1(); } catch ( CB_Web_Session_Migration_Exception $e ) { $failed = $e->getMessage() === 'Session migration unavailable; preserve data and inspect recovery procedure.'; }
	$first = $prefix . 'carmilla_web_families'; $second = $prefix . 'carmilla_web_tokens'; $marker = $prefix . 'carmilla_web_schema';
	$stmt = $db->prepare( 'SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN (?,?,?)' );
	$stmt->bind_param( 'sss', $first, $second, $marker ); $stmt->execute(); $rows = $stmt->get_result()->fetch_all( MYSQLI_ASSOC ); $stmt->close();
	if ( ! $failed || count( $rows ) !== 1 || $rows[0]['TABLE_NAME'] !== $first ) { throw new RuntimeException(); }
	try { $migration->ensure_v1(); throw new RuntimeException(); } catch ( CB_Web_Session_Migration_Exception $e ) { if ( $e->getMessage() !== 'Session migration unavailable; preserve data and inspect recovery procedure.' ) { throw new RuntimeException(); } }
	echo "PASS actual mid-migration DDL permission failure preserved partial schema and retry failed closed\n";
} catch ( Throwable $e ) { fwrite( STDERR, 'Synthetic DDL fault fixture failed: ' . get_class( $e ) . "\n" ); exit( 2 ); }
