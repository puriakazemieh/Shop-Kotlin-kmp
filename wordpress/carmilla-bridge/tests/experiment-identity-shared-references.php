<?php
/** Sequential research fixture only. Never install this schema on a real site. */
$prefix = getenv( 'CARMILLA_LOGIN_TEST_PREFIX' );
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) {
	fwrite( STDERR, "Disposable test flag and random prefix required.\n" ); exit( 2 );
}
define( 'ABSPATH', '/usr/src/wordpress/' ); define( 'WP_INSTALLING', true ); define( 'WP_DEBUG', false );
define( 'DB_NAME', 'carmilla_login_qa' ); define( 'DB_USER', 'synthetic_qa' ); define( 'DB_PASSWORD', 'synthetic_qa_password' ); define( 'DB_HOST', '127.0.0.1:3306' );
define( 'DB_CHARSET', 'utf8mb4' ); define( 'DB_COLLATE', '' ); define( 'WP_HOME', 'https://qa.example.test/wordpress' ); define( 'WP_SITEURL', WP_HOME );
foreach ( array( 'AUTH_KEY', 'SECURE_AUTH_KEY', 'LOGGED_IN_KEY', 'NONCE_KEY', 'AUTH_SALT', 'SECURE_AUTH_SALT', 'LOGGED_IN_SALT', 'NONCE_SALT' ) as $name ) { define( $name, hash( 'sha256', 'synthetic-wp-' . $name ) ); }
$table_prefix = $prefix;
$_SERVER['HTTP_HOST'] = 'qa.example.test'; $_SERVER['SERVER_NAME'] = 'qa.example.test'; $_SERVER['SERVER_PORT'] = '443'; $_SERVER['HTTPS'] = 'on'; $_SERVER['REQUEST_METHOD'] = 'GET';

function shared_check( $condition, $label ) {
	if ( ! $condition ) { echo 'FAIL ' . $label . "\n"; throw new RuntimeException( 'assertion' ); }
	echo 'PASS ' . $label . "\n";
}
function shared_user( $login, $email ) {
	$id = wp_insert_user( array( 'user_login' => $login, 'user_email' => $email, 'user_pass' => 'synthetic-only-password' ) );
	if ( is_wp_error( $id ) || (int) $id < 1 ) { throw new RuntimeException( 'fixture-user' ); }
	return (int) $id;
}
function shared_owner( $key ) {
	global $wpdb, $prefix;
	return (int) $wpdb->get_var( $wpdb->prepare( "SELECT owner_id FROM `{$prefix}identity_claims` WHERE claim_key=%s", $key ) );
}
function shared_refs( $key ) {
	global $wpdb, $prefix;
	return (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM `{$prefix}identity_refs` WHERE claim_key=%s", $key ) );
}
function shared_meta_count( $user, $key ) {
	global $wpdb;
	return (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->usermeta} WHERE user_id=%d AND BINARY meta_key=BINARY %s", $user, $key ) );
}

try {
	require ABSPATH . 'wp-settings.php';
	global $wpdb; $wpdb->hide_errors(); $wpdb->suppress_errors( true );
	require_once ABSPATH . 'wp-admin/includes/upgrade.php';
	add_filter( 'pre_wp_mail', static function () { return true; } );
	wp_install( 'Synthetic QA', 'qa_admin', 'synthetic@example.test', false, '', 'synthetic-only-password' );
	foreach ( array( 'users', 'usermeta' ) as $suffix ) {
		$engine = $wpdb->get_var( $wpdb->prepare( 'SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=%s', $prefix . $suffix ) );
		shared_check( strtoupper( (string) $engine ) === 'INNODB', 'transactional synthetic ' . $suffix );
	}
	$claims = "`{$prefix}identity_claims`"; $refs = "`{$prefix}identity_refs`";
	$users = "`{$prefix}users`"; $meta = "`{$prefix}usermeta`";
	$add = "`{$prefix}identity_add`"; $remove = "`{$prefix}identity_remove`";
	shared_check( $wpdb->query( "CREATE TABLE $claims (claim_key VARBINARY(255) NOT NULL PRIMARY KEY, owner_id BIGINT UNSIGNED NOT NULL) ENGINE=InnoDB" ) !== false, 'create claims table' );
	shared_check( $wpdb->query( "CREATE TABLE $refs (source_kind VARBINARY(16) NOT NULL, source_id BIGINT UNSIGNED NOT NULL, claim_key VARBINARY(255) NOT NULL, owner_id BIGINT UNSIGNED NOT NULL, PRIMARY KEY(source_kind,source_id), KEY(claim_key)) ENGINE=InnoDB" ) !== false, 'create reference table' );
	$sql = "CREATE PROCEDURE $add(IN p_kind VARBINARY(16), IN p_source BIGINT UNSIGNED, IN p_key VARBINARY(255), IN p_owner BIGINT UNSIGNED) BEGIN
		DECLARE v_owner BIGINT UNSIGNED DEFAULT 0;
		INSERT IGNORE INTO $claims (claim_key,owner_id) VALUES (p_key,p_owner);
		SELECT owner_id INTO v_owner FROM $claims WHERE claim_key=p_key FOR UPDATE;
		IF v_owner <> p_owner THEN SIGNAL SQLSTATE '45000'; END IF;
		INSERT INTO $refs (source_kind,source_id,claim_key,owner_id) VALUES(p_kind,p_source,p_key,p_owner);
	END";
	shared_check( $wpdb->query( $sql ) !== false, 'create add-reference procedure' );
	$sql = "CREATE PROCEDURE $remove(IN p_kind VARBINARY(16), IN p_source BIGINT UNSIGNED) BEGIN
		DECLARE v_key VARBINARY(255) DEFAULT NULL; DECLARE v_owner BIGINT UNSIGNED DEFAULT 0; DECLARE v_claim_owner BIGINT UNSIGNED DEFAULT 0;
		SELECT claim_key,owner_id INTO v_key,v_owner FROM $refs WHERE source_kind=p_kind AND source_id=p_source FOR UPDATE;
		IF v_key IS NULL THEN SIGNAL SQLSTATE '45000'; END IF;
		SELECT owner_id INTO v_claim_owner FROM $claims WHERE claim_key=v_key FOR UPDATE;
		IF v_claim_owner <> v_owner THEN SIGNAL SQLSTATE '45000'; END IF;
		DELETE FROM $refs WHERE source_kind=p_kind AND source_id=p_source;
		IF NOT EXISTS(SELECT 1 FROM $refs WHERE claim_key=v_key) THEN DELETE FROM $claims WHERE claim_key=v_key AND owner_id=v_owner; END IF;
	END";
	shared_check( $wpdb->query( $sql ) !== false, 'create remove-reference procedure' );
	$triggers = array(
		'users insert' => "CREATE TRIGGER `{$prefix}identity_users_ai` AFTER INSERT ON $users FOR EACH ROW BEGIN
			CALL $add('login',NEW.ID,CAST(NEW.user_login AS BINARY),NEW.ID);
			IF NEW.user_email <> '' THEN CALL $add('email',NEW.ID,CAST(NEW.user_email AS BINARY),NEW.ID); END IF;
		END",
		'users update' => "CREATE TRIGGER `{$prefix}identity_users_au` AFTER UPDATE ON $users FOR EACH ROW BEGIN
			IF OLD.ID <> NEW.ID THEN SIGNAL SQLSTATE '45000'; END IF;
			IF BINARY OLD.user_login <> BINARY NEW.user_login THEN CALL $remove('login',OLD.ID); CALL $add('login',NEW.ID,CAST(NEW.user_login AS BINARY),NEW.ID); END IF;
			IF BINARY OLD.user_email <> BINARY NEW.user_email THEN
				IF OLD.user_email <> '' THEN CALL $remove('email',OLD.ID); END IF;
				IF NEW.user_email <> '' THEN CALL $add('email',NEW.ID,CAST(NEW.user_email AS BINARY),NEW.ID); END IF;
			END IF;
		END",
		'users delete guard' => "CREATE TRIGGER `{$prefix}identity_users_bd` BEFORE DELETE ON $users FOR EACH ROW BEGIN
			IF EXISTS(SELECT 1 FROM $meta WHERE user_id=OLD.ID AND BINARY meta_key=BINARY 'cb_mobile') THEN SIGNAL SQLSTATE '45000'; END IF;
		END",
		'users delete' => "CREATE TRIGGER `{$prefix}identity_users_ad` AFTER DELETE ON $users FOR EACH ROW BEGIN
			CALL $remove('login',OLD.ID); IF OLD.user_email <> '' THEN CALL $remove('email',OLD.ID); END IF;
		END",
	);
	// This fixture accepts only exact ASCII eleven-digit synthetic mobile values, not a production phone policy.
	$validate = "IF NEW.meta_value IS NULL OR OCTET_LENGTH(NEW.meta_value) <> 11 OR NEW.meta_value NOT REGEXP '^[0-9]{11}$' OR NOT EXISTS(SELECT 1 FROM $users WHERE ID=NEW.user_id) THEN SIGNAL SQLSTATE '45000'; END IF;";
	$triggers['meta insert'] = "CREATE TRIGGER `{$prefix}identity_meta_ai` AFTER INSERT ON $meta FOR EACH ROW BEGIN
		IF BINARY NEW.meta_key=BINARY 'cb_mobile' THEN $validate CALL $add('mobile',NEW.umeta_id,CAST(NEW.meta_value AS BINARY),NEW.user_id); END IF;
	END";
	$triggers['meta update'] = "CREATE TRIGGER `{$prefix}identity_meta_au` AFTER UPDATE ON $meta FOR EACH ROW BEGIN
		IF BINARY OLD.meta_key=BINARY 'cb_mobile' THEN CALL $remove('mobile',OLD.umeta_id); END IF;
		IF BINARY NEW.meta_key=BINARY 'cb_mobile' THEN $validate CALL $add('mobile',NEW.umeta_id,CAST(NEW.meta_value AS BINARY),NEW.user_id); END IF;
	END";
	$triggers['meta delete'] = "CREATE TRIGGER `{$prefix}identity_meta_ad` AFTER DELETE ON $meta FOR EACH ROW BEGIN
		IF BINARY OLD.meta_key=BINARY 'cb_mobile' THEN CALL $remove('mobile',OLD.umeta_id); END IF;
	END";
	foreach ( $triggers as $label => $sql ) { shared_check( $wpdb->query( $sql ) !== false, 'create ' . $label . ' trigger' ); }

	$mobile = '09129990001'; $next = '09129990002'; $direct = '09129990003';
	$a = shared_user( $mobile, 'a@example.test' ); $b = shared_user( 'other_user', 'b@example.test' );
	$first = add_user_meta( $a, 'cb_mobile', $mobile ); $second = add_user_meta( $a, 'cb_mobile', $mobile );
	shared_check( $first && $second && shared_owner( $mobile ) === $a && shared_refs( $mobile ) === 3, 'same owner login and duplicate WordPress mobile references' );
	$result = $wpdb->update( $wpdb->users, array( 'user_login' => 'changed_login' ), array( 'ID' => $a ) );
	shared_check( $result === 1 && shared_owner( $mobile ) === $a && shared_refs( $mobile ) === 2, 'changing login preserves both mobile references' );
	$result = $wpdb->insert( $wpdb->usermeta, array( 'user_id' => $b, 'meta_key' => 'cb_mobile', 'meta_value' => $mobile ) );
	shared_check( $result === false && shared_owner( $mobile ) === $a && shared_refs( $mobile ) === 2 && shared_meta_count( $b, 'cb_mobile' ) === 0, 'other owner cannot claim an existing mobile' );
	shared_check( delete_metadata_by_mid( 'user', $first ) && shared_owner( $mobile ) === $a && shared_refs( $mobile ) === 1, 'deleting one WordPress reference keeps claim' );
	shared_check( update_metadata_by_mid( 'user', $second, $next, 'cb_mobile' ) && shared_owner( $mobile ) === 0 && shared_owner( $next ) === $a && shared_refs( $next ) === 1, 'updating last WordPress reference releases only old claim' );
	$result = $wpdb->insert( $wpdb->usermeta, array( 'user_id' => $a, 'meta_key' => 'cb_mobile', 'meta_value' => $direct ) ); $mid = (int) $wpdb->insert_id;
	shared_check( $result === 1 && $mid > 0 && shared_owner( $direct ) === $a && shared_refs( $direct ) === 1, 'direct metadata insert tracked' );
	$result = $wpdb->update( $wpdb->usermeta, array( 'meta_value' => $next ), array( 'umeta_id' => $mid ) );
	shared_check( $result === 1 && shared_owner( $direct ) === 0 && shared_owner( $next ) === $a && shared_refs( $next ) === 2, 'direct update joins existing same-owner claim' );
	$result = $wpdb->delete( $wpdb->usermeta, array( 'umeta_id' => $mid ) );
	shared_check( $result === 1 && shared_owner( $next ) === $a && shared_refs( $next ) === 1, 'direct delete preserves remaining reference' );
	shared_check( delete_user_meta( $a, 'cb_mobile', $next ) && shared_owner( $next ) === 0 && shared_refs( $next ) === 0, 'WordPress delete removes last reference and claim' );
	add_user_meta( $a, 'cb_mobile', $next ); add_user_meta( $a, 'cb_mobile', $next );
	$result = update_user_meta( $a, 'cb_mobile', $direct, $next );
	shared_check( $result !== false && shared_owner( $next ) === 0 && shared_owner( $direct ) === $a && shared_refs( $direct ) === 2, 'WordPress update moves duplicate mobile rows' );
	$result = delete_user_meta( $a, 'cb_mobile', $direct );
	shared_check( $result && shared_owner( $direct ) === 0 && shared_refs( $direct ) === 0, 'WordPress delete removes duplicate mobile rows' );
	$alias = shared_user( 'alias@example.test', 'alias@example.test' );
	shared_check( shared_owner( 'alias@example.test' ) === $alias && shared_refs( 'alias@example.test' ) === 2, 'same-owner login and email share one claim' );
	$result = wp_update_user( array( 'ID' => $alias, 'user_email' => 'newalias@example.test' ) );
	shared_check( ! is_wp_error( $result ) && shared_owner( 'alias@example.test' ) === $alias && shared_refs( 'alias@example.test' ) === 1 && shared_owner( 'newalias@example.test' ) === $alias, 'WordPress email update preserves login reference' );
	shared_check( add_user_meta( $b, 'billing_phone', $mobile ) && shared_owner( $mobile ) === 0 && shared_refs( $mobile ) === 0, 'billing phone is excluded from identity claims' );
	foreach ( array( '', '123', 'not-a-phone', null ) as $invalid ) {
		$result = $wpdb->insert( $wpdb->usermeta, array( 'user_id' => $b, 'meta_key' => 'cb_mobile', 'meta_value' => $invalid ) );
		if ( $result !== false ) { throw new RuntimeException( 'invalid-mobile' ); }
	}
	shared_check( shared_meta_count( $b, 'cb_mobile' ) === 0, 'unsupported synthetic mobile values rejected' );
	$guarded = add_user_meta( $b, 'cb_mobile', $mobile );
	$conflict = add_user_meta( $a, 'cb_mobile', $next );
	$result = $wpdb->update( $wpdb->usermeta, array( 'meta_value' => $mobile ), array( 'umeta_id' => $conflict ) );
	shared_check( $result === false && shared_owner( $mobile ) === $b && shared_owner( $next ) === $a && shared_refs( $next ) === 1, 'conflicting metadata update rolls back previous reference' );
	$result = $wpdb->update( $wpdb->usermeta, array( 'meta_value' => '' ), array( 'umeta_id' => $conflict ) );
	shared_check( $result === false && shared_owner( $next ) === $a && shared_refs( $next ) === 1, 'invalid metadata update preserves previous reference' );
	$wpdb->query( 'START TRANSACTION' ); $result = $wpdb->delete( $wpdb->usermeta, array( 'umeta_id' => $conflict ) ); $wpdb->query( 'ROLLBACK' );
	shared_check( $result === 1 && shared_owner( $next ) === $a && shared_refs( $next ) === 1, 'transaction rollback restores metadata reference' );
	$result = $wpdb->delete( $wpdb->users, array( 'ID' => $b ) );
	shared_check( $guarded && $result === false && shared_owner( $mobile ) === $b && shared_refs( $mobile ) === 1, 'direct user delete with mobile fails closed' );
	shared_check( delete_user_meta( $b, 'cb_mobile', $mobile ) && $wpdb->delete( $wpdb->users, array( 'ID' => $b ) ) === 1 && shared_owner( $mobile ) === 0 && shared_owner( 'other_user' ) === 0 && shared_owner( 'b@example.test' ) === 0, 'user delete after mobile removal releases final claims' );
	require_once ABSPATH . 'wp-admin/includes/user.php';
	shared_check( wp_delete_user( $a ) && shared_owner( $next ) === 0 && shared_owner( 'changed_login' ) === 0 && shared_owner( 'a@example.test' ) === 0, 'WordPress user delete removes metadata then final claims' );
	echo "Sequential synthetic reference model only; not production protection.\n";
} catch ( Throwable $e ) {
	$known = array( 'assertion', 'fixture-user', 'invalid-mobile' );
	$category = in_array( $e->getMessage(), $known, true ) ? $e->getMessage() : get_class( $e );
	fwrite( STDERR, 'FAIL synthetic reference experiment: ' . $category . "\n" ); exit( 1 );
}
