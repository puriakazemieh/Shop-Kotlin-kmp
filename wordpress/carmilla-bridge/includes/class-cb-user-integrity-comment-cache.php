<?php
/** Keep WordPress comment objects coherent when the DB trigger anonymizes them. */
if ( ! defined( 'ABSPATH' ) ) { exit; }

final class CB_User_Integrity_Comment_Cache {
	private static $pending = array();

	public static function boot(): void {
		add_action( 'delete_user', array( __CLASS__, 'before_delete' ), 5, 1 );
		add_action( 'deleted_user', array( __CLASS__, 'after_delete' ), 5, 1 );
	}

	public static function before_delete( int $id ): void {
		global $wpdb;
		if ( CB_User_Integrity::inspect()['state'] !== 'installed' ) { return; }
		$wpdb->last_error = '';
		$ids = $wpdb->get_col( $wpdb->prepare( "SELECT comment_ID FROM {$wpdb->comments} WHERE user_id=%d", $id ) );
		if ( $wpdb->last_error !== '' || ! is_array( $ids ) ) { throw new RuntimeException( 'Comment cache inventory unavailable; deletion stopped.' ); }
		self::$pending[ $id ] = array_map( 'intval', $ids );
	}

	public static function after_delete( int $id ): void {
		foreach ( self::$pending[ $id ] ?? array() as $comment_id ) { clean_comment_cache( $comment_id ); }
		unset( self::$pending[ $id ] );
	}
}
