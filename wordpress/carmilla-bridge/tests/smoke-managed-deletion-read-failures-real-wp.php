<?php
/** Disposable installed WordPress only; injects SQL read failures for synthetic users. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';

global $wpdb;
$wpdb->hide_errors();
$failed = 0;
$check = static function ( bool $ok, string $label ) use ( &$failed ): void {
	if ( ! $ok ) { ++$failed; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n";
};
$service = new CB_Managed_User_Deletion();
$delete = new ReflectionMethod( $service, 'delete' );
$delete->setAccessible( true );
$ids = array();
$make_user = static function () use ( &$ids ): int {
	$id = wp_create_user( 'qa_delete_' . bin2hex( random_bytes( 6 ) ), wp_generate_password( 24 ), 'qa-delete-' . bin2hex( random_bytes( 6 ) ) . '@example.test' );
	if ( ! is_int( $id ) ) { throw new RuntimeException( 'Synthetic user unavailable.' ); }
	add_user_meta( $id, 'cb_identity_managed', '1' );
	$ids[] = $id;
	return $id;
};
$inject = static function ( string $table, string $column, int $id ) use ( $wpdb ): callable {
	$needle = "SELECT COUNT(*) FROM {$table} WHERE {$column}={$id}";
	return static function ( string $sql ) use ( $needle ): string {
		return strpos( $sql, $needle ) !== false ? 'SELECT COUNT(*) FROM cb_synthetic_missing_table' : $sql;
	};
};

try {
	foreach ( array( array( $wpdb->posts, 'post_author', 'post dependency' ), array( $wpdb->comments, 'user_id', 'comment dependency' ), array( $wpdb->users, 'ID', 'post-delete verification' ) ) as $case ) {
		$id = $make_user();
		$filter = $inject( $case[0], $case[1], $id );
		add_filter( 'query', $filter );
		$status = $delete->invoke( $service, $id );
		remove_filter( 'query', $filter );
		$check( $status === 'unavailable', $case[2] . ' failure is unavailable' );
		$check( get_user_by( 'id', $id ) instanceof WP_User && metadata_exists( 'user', $id, 'cb_identity_managed' ), $case[2] . ' failure preserves user and metadata' );
	}
	$id = $make_user();
	$check( $delete->invoke( $service, $id ) === 'deleted' && ! get_user_by( 'id', $id ), 'healthy managed deletion still commits' );
} finally {
	foreach ( $ids as $id ) {
		$wpdb->delete( $wpdb->usermeta, array( 'user_id' => $id ), array( '%d' ) );
		$wpdb->delete( $wpdb->users, array( 'ID' => $id ), array( '%d' ) );
		clean_user_cache( $id );
	}
}
echo "Failures: {$failed}.\n";
exit( $failed ? 1 : 0 );
