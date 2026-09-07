<?php
namespace Carmilla\Core\Migration;

/**
 * Shared Schema and Migration Runner.
 * Ensures the core database tables, options, and CPT checkpoints are migrated
 * correctly regardless of whether the Plugin, the Theme, or Both are installed.
 */
class SchemaRunner {
    
    private const SCHEMA_VERSION_KEY = 'carmilla_schema_version';
    private const SCHEMA_LOCK_KEY = 'carmilla_schema_lock';
    private const CURRENT_SCHEMA_VERSION = '1.0.1';

    /**
     * Run the schema migrations if necessary.
     */
    public static function run_migrations() {
        $current_version = get_option(self::SCHEMA_VERSION_KEY, '0.0.0');

        if (version_compare($current_version, self::CURRENT_SCHEMA_VERSION, '<')) {
            if (self::acquire_lock()) {
                try {
                    self::migrate_up($current_version);
                    update_option(self::SCHEMA_VERSION_KEY, self::CURRENT_SCHEMA_VERSION);
                } finally {
                    self::release_lock();
                }
            }
        }
    }

    private static function acquire_lock(): bool {
        $lock = get_transient(self::SCHEMA_LOCK_KEY);
        if ($lock) {
            return false;
        }
        set_transient(self::SCHEMA_LOCK_KEY, time(), 60); // 60 seconds lock
        return true;
    }

    private static function release_lock(): void {
        delete_transient(self::SCHEMA_LOCK_KEY);
    }

    private static function migrate_up(string $from_version) {
        if (version_compare($from_version, '1.0.0', '<')) {
            self::migrate_to_1_0_0();
        }
        if (version_compare($from_version, '1.0.1', '<')) {
            self::migrate_to_1_0_1();
        }
    }

    private static function migrate_to_1_0_0() {
        // Shared schema baseline (e.g. tracking options)
        add_option('carmilla_core_installed', time());
    }

    private static function migrate_to_1_0_1() {
        global $wpdb;
        $charset_collate = $wpdb->get_charset_collate();

        $sql = "CREATE TABLE {$wpdb->prefix}carmilla_seed_runs (
            id bigint(20) unsigned NOT NULL AUTO_INCREMENT,
            pack_id varchar(100) NOT NULL,
            checksum varchar(32) NOT NULL,
            run_date datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
            PRIMARY KEY  (id),
            KEY pack_id (pack_id)
        ) $charset_collate;
        
        CREATE TABLE {$wpdb->prefix}carmilla_seed_objects (
            id bigint(20) unsigned NOT NULL AUTO_INCREMENT,
            pack_id varchar(100) NOT NULL,
            feature varchar(100) NOT NULL,
            wp_entity_type varchar(50) NOT NULL,
            wp_entity_id bigint(20) unsigned NOT NULL,
            seed_entity_id varchar(100) NOT NULL,
            PRIMARY KEY  (id),
            KEY pack_feature (pack_id, feature),
            KEY seed_entity (seed_entity_id)
        ) $charset_collate;";

        require_once(ABSPATH . 'wp-admin/includes/upgrade.php');
        dbDelta($sql);
    }

    /**
     * Checkpoint registration for CPTs and capabilities.
     * Prevents duplication across multiple boots.
     */
    public static function register_checkpoint(string $checkpoint_name): bool {
        $checkpoints = get_option('carmilla_boot_checkpoints', []);
        
        if (in_array($checkpoint_name, $checkpoints, true)) {
            return false; // Already registered in this cycle
        }
        
        $checkpoints[] = $checkpoint_name;
        update_option('carmilla_boot_checkpoints', $checkpoints);
        return true;
    }

    /**
     * Resets checkpoints (usually hooked to shutdown or init start).
     */
    public static function reset_checkpoints() {
        delete_option('carmilla_boot_checkpoints');
    }
}
