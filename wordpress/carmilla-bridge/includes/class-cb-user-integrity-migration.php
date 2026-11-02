<?php
/** Explicit migration core; no activation or request hook calls this class. */
if ( ! defined( 'ABSPATH' ) ) { exit; }

final class CB_User_Integrity_Migration {
	/** DDL is not transactional. A failed second ALTER leaves a partial state for operator recovery. */
	public static function install( ?array $tables = null ): array {
		global $wpdb;
		$state = CB_User_Integrity::inspect( $tables );
		if ( $state['state'] === 'installed' ) { return array( 'status' => 'installed', 'changed' => false ); }
		if ( $state['state'] !== 'uninstalled' ) { return array( 'status' => 'refused', 'reason' => $state['state'] . ':' . $state['reason'] ); }
		$tables = $tables ?? array( 'users' => $wpdb->users, 'posts' => $wpdb->posts, 'comments' => $wpdb->comments );
		$names = CB_User_Integrity::names( $tables['posts'], $tables['comments'] );
		$steps = array(
			array( $tables['posts'], 'post_author', $names['posts_column'], $names['posts_fk'] ),
			array( $tables['comments'], 'user_id', $names['comments_column'], $names['comments_fk'] ),
		);
		foreach ( $steps as $step ) {
			list( $table, $source, $column, $fk ) = $step;
			$wpdb->last_error = '';
			$sql = "ALTER TABLE `$table` ADD COLUMN `$column` BIGINT UNSIGNED GENERATED ALWAYS AS (NULLIF(`$source`,0)) PERSISTENT, ADD INDEX (`$column`), ADD CONSTRAINT `$fk` FOREIGN KEY (`$column`) REFERENCES `{$tables['users']}` (`ID`) ON DELETE RESTRICT";
			if ( $wpdb->query( $sql ) === false || $wpdb->last_error !== '' ) {
				$after = CB_User_Integrity::inspect( $tables );
				return array( 'status' => 'failed', 'reason' => 'ddl', 'state' => $after['state'] );
			}
		}
		$wpdb->last_error = '';
		$trigger = "CREATE TRIGGER `{$names['users_trigger']}` BEFORE DELETE ON `{$tables['users']}` FOR EACH ROW UPDATE `{$tables['comments']}` SET user_id=0 WHERE `{$names['comments_column']}`=OLD.ID";
		if ( $wpdb->query( $trigger ) === false || $wpdb->last_error !== '' ) {
			$after = CB_User_Integrity::inspect( $tables );
			return array( 'status' => 'failed', 'reason' => 'trigger_ddl', 'state' => $after['state'] );
		}
		$after = CB_User_Integrity::inspect( $tables );
		return $after['state'] === 'installed'
			? array( 'status' => 'installed', 'changed' => true )
			: array( 'status' => 'failed', 'reason' => 'postcheck', 'state' => $after['state'] );
	}
}
