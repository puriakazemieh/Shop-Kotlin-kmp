<?php
/** Read-only database contract for managed user deletion. */
if ( ! defined( 'ABSPATH' ) ) { exit; }

final class CB_User_Integrity {
	public static function names( string $posts, string $comments ): array {
		return array(
			'posts_column' => 'cb_author_user_ref',
			'comments_column' => 'cb_comment_user_ref',
			'posts_fk' => 'cb_author_fk_' . substr( sha1( $posts ), 0, 12 ),
			'comments_fk' => 'cb_comment_fk_' . substr( sha1( $comments ), 0, 12 ),
			'users_trigger' => 'cb_comment_zero_' . substr( sha1( $posts ), 0, 12 ),
		);
	}

	/** Returns state: installed, uninstalled, partial, unsupported, or unreadable. */
	public static function inspect( ?array $tables = null ): array {
		global $wpdb;
		if ( is_multisite() ) { return array( 'state' => 'unsupported', 'reason' => 'multisite' ); }
		$tables = $tables ?? array( 'users' => $wpdb->users, 'posts' => $wpdb->posts, 'comments' => $wpdb->comments );
		if ( array_keys( $tables ) !== array( 'users', 'posts', 'comments' ) || count( array_filter( $tables, static function ( $name ) { return is_string( $name ) && preg_match( '/^[A-Za-z0-9_]+$/D', $name ); } ) ) !== 3 ) {
			return array( 'state' => 'unsupported', 'reason' => 'table_names' );
		}
		$version = self::read_var( 'SELECT VERSION()' );
		if ( $version === null ) { return array( 'state' => 'unreadable', 'reason' => 'version' ); }
		if ( ! preg_match( '/^10\.(?:[6-9]|[1-9][0-9])\.[0-9]+.*MariaDB/i', $version ) ) { return array( 'state' => 'unsupported', 'reason' => 'db_version' ); }
		$names = self::names( $tables['posts'], $tables['comments'] );
		foreach ( $tables as $kind => $table ) {
			$row = self::read_row( $wpdb->prepare( 'SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=%s', $table ) );
			if ( $row === null ) { return array( 'state' => 'unreadable', 'reason' => 'table_metadata' ); }
			if ( ! $row || $row['ENGINE'] !== 'InnoDB' ) { return array( 'state' => 'unsupported', 'reason' => $kind . '_engine' ); }
		}
		foreach ( array( array( 'users', 'ID' ), array( 'posts', 'post_author' ), array( 'comments', 'user_id' ) ) as $required ) {
			$row = self::read_row( $wpdb->prepare( 'SELECT DATA_TYPE,COLUMN_TYPE,IS_NULLABLE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=%s AND COLUMN_NAME=%s', $tables[ $required[0] ], $required[1] ) );
			if ( $row === null ) { return array( 'state' => 'unreadable', 'reason' => 'column_metadata' ); }
			if ( ! $row || strtolower( $row['DATA_TYPE'] ) !== 'bigint' || stripos( $row['COLUMN_TYPE'], 'unsigned' ) === false || $row['IS_NULLABLE'] !== 'NO' ) { return array( 'state' => 'unsupported', 'reason' => $required[0] . '_column' ); }
		}
		foreach ( array( 'posts' => 'post_author', 'comments' => 'user_id' ) as $kind => $column ) {
			$table = $tables[ $kind ];
			$count = self::read_var( "SELECT COUNT(*) FROM `$table` child LEFT JOIN `{$tables['users']}` parent ON parent.ID=child.`$column` WHERE child.`$column`<>0 AND parent.ID IS NULL" );
			if ( $count === null || ! ctype_digit( $count ) ) { return array( 'state' => 'unreadable', 'reason' => 'orphan_count' ); }
			if ( (int) $count > 0 ) { return array( 'state' => 'unsupported', 'reason' => $kind . '_orphans' ); }
		}
		$present = 0;
		foreach ( array( 'posts' => array( $names['posts_column'], $names['posts_fk'], 'post_author' ), 'comments' => array( $names['comments_column'], $names['comments_fk'], 'user_id' ) ) as $kind => $spec ) {
			$row = self::read_row( $wpdb->prepare( 'SELECT DATA_TYPE,COLUMN_TYPE,IS_NULLABLE,GENERATION_EXPRESSION FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=%s AND COLUMN_NAME=%s', $tables[ $kind ], $spec[0] ) );
			if ( $row === null ) { return array( 'state' => 'unreadable', 'reason' => 'generated_metadata' ); }
			$fk = self::read_row( $wpdb->prepare( 'SELECT k.REFERENCED_TABLE_NAME,k.REFERENCED_COLUMN_NAME,r.DELETE_RULE FROM information_schema.KEY_COLUMN_USAGE k JOIN information_schema.REFERENTIAL_CONSTRAINTS r ON r.CONSTRAINT_SCHEMA=k.CONSTRAINT_SCHEMA AND r.CONSTRAINT_NAME=k.CONSTRAINT_NAME WHERE k.TABLE_SCHEMA=DATABASE() AND k.TABLE_NAME=%s AND k.CONSTRAINT_NAME=%s AND k.COLUMN_NAME=%s', $tables[ $kind ], $spec[1], $spec[0] ) );
			if ( $fk === null ) { return array( 'state' => 'unreadable', 'reason' => 'fk_metadata' ); }
			if ( $row || $fk ) {
				++$present;
				$expression = $row ? strtolower( preg_replace( '/[`\s()]/', '', $row['GENERATION_EXPRESSION'] ) ) : '';
				if ( ! $row || ! $fk || strtolower( $row['DATA_TYPE'] ) !== 'bigint' || stripos( $row['COLUMN_TYPE'], 'unsigned' ) === false || $row['IS_NULLABLE'] !== 'YES' || $expression !== 'nullif' . $spec[2] . ',0' || $fk['REFERENCED_TABLE_NAME'] !== $tables['users'] || $fk['REFERENCED_COLUMN_NAME'] !== 'ID' || $fk['DELETE_RULE'] !== 'RESTRICT' ) {
					return array( 'state' => 'partial', 'reason' => $kind . '_constraint' );
				}
			}
		}
		$other = self::read_var( $wpdb->prepare( "SELECT COUNT(*) FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA=DATABASE() AND EVENT_OBJECT_TABLE=%s AND ACTION_TIMING='BEFORE' AND EVENT_MANIPULATION='DELETE' AND TRIGGER_NAME<>%s", $tables['users'], $names['users_trigger'] ) );
		if ( $other === null || ! ctype_digit( $other ) ) { return array( 'state' => 'unreadable', 'reason' => 'trigger_metadata' ); }
		if ( (int) $other > 0 ) { return array( 'state' => 'unsupported', 'reason' => 'conflicting_user_trigger' ); }
		$trigger = self::read_row( $wpdb->prepare( 'SELECT ACTION_TIMING,EVENT_MANIPULATION,ACTION_STATEMENT FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA=DATABASE() AND EVENT_OBJECT_TABLE=%s AND TRIGGER_NAME=%s', $tables['users'], $names['users_trigger'] ) );
		if ( $trigger === null ) { return array( 'state' => 'unreadable', 'reason' => 'trigger_metadata' ); }
		if ( $trigger ) {
			++$present;
			$statement = strtolower( preg_replace( '/[`\s()]/', '', $trigger['ACTION_STATEMENT'] ) );
			$expected = 'update' . strtolower( $tables['comments'] ) . 'setuser_id=0where' . $names['comments_column'] . '=old.id';
			if ( $trigger['ACTION_TIMING'] !== 'BEFORE' || $trigger['EVENT_MANIPULATION'] !== 'DELETE' || $statement !== $expected ) { return array( 'state' => 'partial', 'reason' => 'user_trigger' ); }
		}
		return array( 'state' => $present === 0 ? 'uninstalled' : ( $present === 3 ? 'installed' : 'partial' ), 'reason' => $present === 0 || $present === 3 ? 'verified' : 'incomplete_constraints' );
	}

	private static function read_var( string $sql ): ?string {
		global $wpdb;
		$wpdb->last_error = '';
		$value = $wpdb->get_var( $sql );
		return $wpdb->last_error === '' && is_scalar( $value ) ? (string) $value : null;
	}

	private static function read_row( string $sql ): ?array {
		global $wpdb;
		$wpdb->last_error = '';
		$row = $wpdb->get_row( $sql, ARRAY_A );
		return $wpdb->last_error === '' ? ( $row ?: array() ) : null;
	}
}
