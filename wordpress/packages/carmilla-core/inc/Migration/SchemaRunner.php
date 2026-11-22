<?php
namespace Carmilla\Core\Migration;

/**
 * Shared Schema and Resumable Migration Runner.
 *
 * Ensures core database tables, options, and checkpoints are migrated
 * deterministically and idempotently across Carmilla Theme, Carmilla Bridge (Plugin),
 * or co-installed environments.
 *
 * Guarantees:
 * 1. Single kernel implementation: only one runner executes migrations.
 * 2. Distributed locking: prevents concurrent race conditions across processes.
 * 3. Stale lock recovery: breaks locks older than expiration window.
 * 4. Resumable step-level checkpoints: failed migrations resume without duplicating data.
 * 5. Downgrade prevention: strictly rejects incompatible schema downgrades without writing.
 * 6. Co-install idempotency: multiple hosts in the same or sequential cycles never duplicate migrations.
 *
 * Part of Task P04-WPPLUGIN-CODE-003.
 */
class SchemaRunner {

    public const SCHEMA_VERSION_KEY = 'carmilla_schema_version';
    public const SCHEMA_LOCK_KEY = 'carmilla_schema_lock';
    public const CHECKPOINTS_KEY = 'carmilla_schema_checkpoints';
    public const CURRENT_SCHEMA_VERSION = '1.0.3';
    public const LOCK_TIMEOUT_SECONDS = 60;

    /**
     * In-memory checkpoints for the current request lifecycle.
     *
     * @var array<string, bool>
     */
    private static $in_memory_checkpoints = [];

    /**
     * Fault injector callback or flag for testing synthetic migration recovery.
     *
     * @var callable|bool|null
     */
    private static $fault_injector = null;

    /**
     * Runs schema migrations up to target version.
     *
     * @param array $options Optional execution flags ('target_version', 'force_run', 'lock_timeout').
     * @return array Result metadata including success status and version details.
     */
    public static function run_migrations(array $options = []): array {
        // 1. In-memory co-install deduplication for the current request cycle
        $force_run = !empty($options['force_run']);
        if (!$force_run && !self::register_checkpoint('run_migrations_cycle')) {
            return [
                'success' => true,
                'status'  => 'skipped',
                'reason'  => 'already_run_in_request',
                'version' => self::get_current_schema_version()
            ];
        }

        $current_version = self::get_current_schema_version();
        $target_version  = $options['target_version'] ?? self::CURRENT_SCHEMA_VERSION;
        $lock_timeout    = $options['lock_timeout'] ?? self::LOCK_TIMEOUT_SECONDS;

        // 2. Incompatible downgrade prevention (fail-closed, no writes)
        if (version_compare($current_version, $target_version, '>')) {
            return [
                'success' => false,
                'status'  => 'rejected',
                'error'   => 'incompatible_downgrade_rejected',
                'current' => $current_version,
                'target'  => $target_version
            ];
        }

        // 3. Schema is already current (noop)
        if (version_compare($current_version, $target_version, '=')) {
            return [
                'success' => true,
                'status'  => 'noop',
                'reason'  => 'already_up_to_date',
                'version' => $current_version
            ];
        }

        // 4. Acquire distributed lock with unique execution token
        $lock_token = uniqid('mig_lock_', true);
        if (!self::acquire_lock($lock_token, $lock_timeout)) {
            return [
                'success' => false,
                'status'  => 'locked',
                'error'   => 'migration_in_progress'
            ];
        }

        // 5. Execute migrations step-by-step
        try {
            self::migrate_up($current_version, $target_version);
            update_option(self::SCHEMA_VERSION_KEY, $target_version);

            return [
                'success' => true,
                'status'  => 'migrated',
                'from'    => $current_version,
                'to'      => $target_version
            ];
        } catch (\Throwable $e) {
            return [
                'success'         => false,
                'status'          => 'failed',
                'error'           => $e->getMessage(),
                'current_version' => self::get_current_schema_version()
            ];
        } finally {
            self::release_lock($lock_token);
        }
    }

    /**
     * Returns the currently recorded schema version in WordPress options.
     *
     * @return string
     */
    public static function get_current_schema_version(): string {
        return (string) get_option(self::SCHEMA_VERSION_KEY, '0.0.0');
    }

    /**
     * Configures a fault injector for testing mid-migration failure and recovery.
     *
     * @param callable|bool|null $injector
     * @return void
     */
    public static function set_fault_injector($injector): void {
        self::$fault_injector = $injector;
    }

    /**
     * Executes migration batches in strictly increasing semver order.
     *
     * @param string $from_version
     * @param string $to_version
     * @return void
     */
    private static function migrate_up(string $from_version, string $to_version): void {
        if (version_compare($from_version, '1.0.0', '<') && version_compare($to_version, '1.0.0', '>=')) {
            self::migrate_to_1_0_0();
        }
        if (version_compare($from_version, '1.0.1', '<') && version_compare($to_version, '1.0.1', '>=')) {
            self::migrate_to_1_0_1();
        }
        if (version_compare($from_version, '1.0.2', '<') && version_compare($to_version, '1.0.2', '>=')) {
            self::migrate_to_1_0_2();
        }
        if (version_compare($from_version, '1.0.3', '<') && version_compare($to_version, '1.0.3', '>=')) {
            self::migrate_to_1_0_3();
        }
    }

    /**
     * Version 1.0.0: Baseline option registration.
     */
    private static function migrate_to_1_0_0(): void {
        self::run_resumable_step('1.0.0_baseline_install', function() {
            add_option('carmilla_core_installed', time());
        });
    }

    /**
     * Version 1.0.1: Seed tables.
     */
    private static function migrate_to_1_0_1(): void {
        self::run_resumable_step('1.0.1_create_seed_tables', function() {
            global $wpdb;
            if (!$wpdb) {
                return;
            }
            $charset_collate = method_exists($wpdb, 'get_charset_collate') ? $wpdb->get_charset_collate() : '';

            $sql = "CREATE TABLE IF NOT EXISTS {$wpdb->prefix}carmilla_seed_runs (
                id bigint(20) unsigned NOT NULL AUTO_INCREMENT,
                pack_id varchar(100) NOT NULL,
                checksum varchar(32) NOT NULL,
                run_date datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
                PRIMARY KEY (id),
                KEY pack_id (pack_id)
            ) $charset_collate;
            CREATE TABLE IF NOT EXISTS {$wpdb->prefix}carmilla_seed_objects (
                id bigint(20) unsigned NOT NULL AUTO_INCREMENT,
                pack_id varchar(100) NOT NULL,
                feature varchar(100) NOT NULL,
                wp_entity_type varchar(50) NOT NULL,
                wp_entity_id bigint(20) unsigned NOT NULL,
                seed_entity_id varchar(100) NOT NULL,
                PRIMARY KEY (id),
                KEY pack_feature (pack_id, feature),
                KEY seed_entity (seed_entity_id)
            ) $charset_collate;";

            if (function_exists('dbDelta')) {
                require_once(ABSPATH . 'wp-admin/includes/upgrade.php');
                dbDelta($sql);
            } elseif (method_exists($wpdb, 'query')) {
                $wpdb->query($sql);
            }
        });
    }

    /**
     * Version 1.0.2: Seed objects index upgrade.
     */
    private static function migrate_to_1_0_2(): void {
        self::run_resumable_step('1.0.2_upgrade_seed_indexes', function() {
            global $wpdb;
            if (!$wpdb) {
                return;
            }
            $charset_collate = method_exists($wpdb, 'get_charset_collate') ? $wpdb->get_charset_collate() : '';
            $table = $wpdb->prefix . 'carmilla_seed_objects';

            $sql = "CREATE TABLE IF NOT EXISTS {$table} (
                id bigint(20) unsigned NOT NULL AUTO_INCREMENT,
                pack_id varchar(100) NOT NULL,
                feature varchar(100) NOT NULL,
                wp_entity_type varchar(50) NOT NULL,
                wp_entity_id bigint(20) unsigned NOT NULL,
                seed_entity_id varchar(100) NOT NULL,
                created_at datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
                PRIMARY KEY (id),
                KEY pack_feature (pack_id, feature),
                KEY seed_entity (seed_entity_id),
                KEY wp_entity_id (wp_entity_id)
            ) $charset_collate;";

            if (function_exists('dbDelta')) {
                require_once(ABSPATH . 'wp-admin/includes/upgrade.php');
                dbDelta($sql);
            } elseif (method_exists($wpdb, 'query')) {
                $wpdb->query($sql);
            }
        });
    }

    /**
     * Version 1.0.3: Resumable Synthetic Migration with Step Checkpoints.
     * Demonstrates clean install, mid-migration fault, and idempotent resume without duplicate rows.
     */
    private static function migrate_to_1_0_3(): void {
        // Step 1: Create synthetic test table
        self::run_resumable_step('1.0.3_create_synthetic_table', function() {
            global $wpdb;
            if (!$wpdb) {
                return;
            }
            $charset_collate = method_exists($wpdb, 'get_charset_collate') ? $wpdb->get_charset_collate() : '';
            $table = $wpdb->prefix . 'carmilla_synthetic_test';

            $sql = "CREATE TABLE IF NOT EXISTS {$table} (
                id bigint(20) unsigned NOT NULL AUTO_INCREMENT,
                item_key varchar(100) NOT NULL UNIQUE,
                item_val text NOT NULL,
                created_at datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
                PRIMARY KEY (id)
            ) $charset_collate;";

            if (function_exists('dbDelta')) {
                require_once(ABSPATH . 'wp-admin/includes/upgrade.php');
                dbDelta($sql);
            } elseif (method_exists($wpdb, 'query')) {
                $wpdb->query($sql);
            }
        });

        // Step 2: Insert initial seed entry Alpha
        self::run_resumable_step('1.0.3_insert_seed_alpha', function() {
            global $wpdb;
            if (!$wpdb) {
                return;
            }
            $table = $wpdb->prefix . 'carmilla_synthetic_test';
            if (method_exists($wpdb, 'insert')) {
                $wpdb->insert($table, [
                    'item_key' => 'synthetic_alpha',
                    'item_val' => 'Alpha initial entry'
                ]);
            }
        });

        // Step 3: Insert seed entry Beta with potential fault injection
        self::run_resumable_step('1.0.3_insert_seed_beta', function() {
            // Fault injection check
            if (is_callable(self::$fault_injector)) {
                call_user_func(self::$fault_injector, '1.0.3_insert_seed_beta');
            } elseif (self::$fault_injector === true) {
                throw new \RuntimeException("Synthetic migration fault injected at step 1.0.3_insert_seed_beta");
            }

            global $wpdb;
            if (!$wpdb) {
                return;
            }
            $table = $wpdb->prefix . 'carmilla_synthetic_test';
            if (method_exists($wpdb, 'insert')) {
                $wpdb->insert($table, [
                    'item_key' => 'synthetic_beta',
                    'item_val' => 'Beta completed entry'
                ]);
            }
        });
    }

    /**
     * Executes a callback within a resumable checkpoint.
     * If the step was previously marked completed, it is skipped.
     *
     * @param string   $step_key
     * @param callable $callback
     * @return void
     */
    public static function run_resumable_step(string $step_key, callable $callback): void {
        if (self::is_step_completed($step_key)) {
            return;
        }

        $callback();
        self::mark_step_completed($step_key);
    }

    /**
     * Checks if a persistent migration step has already succeeded.
     *
     * @param string $step_key
     * @return bool
     */
    public static function is_step_completed(string $step_key): bool {
        $checkpoints = self::get_persisted_checkpoints();
        return !empty($checkpoints[$step_key]);
    }

    /**
     * Marks a persistent migration step as completed.
     *
     * @param string $step_key
     * @return void
     */
    public static function mark_step_completed(string $step_key): void {
        $checkpoints = self::get_persisted_checkpoints();
        $checkpoints[$step_key] = time();
        update_option(self::CHECKPOINTS_KEY, $checkpoints);
    }

    /**
     * Retrieves the map of all persisted migration checkpoints.
     *
     * @return array<string, int>
     */
    public static function get_persisted_checkpoints(): array {
        $checkpoints = get_option(self::CHECKPOINTS_KEY, []);
        return is_array($checkpoints) ? $checkpoints : [];
    }

    /**
     * Clears all persisted migration checkpoints (for test isolation and rollback).
     *
     * @return void
     */
    public static function clear_persisted_checkpoints(): void {
        delete_option(self::CHECKPOINTS_KEY);
    }

    /**
     * Acquires a distributed lock with automatic stale-lock recovery.
     *
     * @param string $lock_token
     * @param int    $timeout
     * @return bool True if lock was acquired, false if held by another active process.
     */
    public static function acquire_lock(string $lock_token, int $timeout = self::LOCK_TIMEOUT_SECONDS): bool {
        $lock = get_transient(self::SCHEMA_LOCK_KEY);
        $now  = time();

        if ($lock) {
            $acquired_at = is_array($lock) && isset($lock['time']) ? (int) $lock['time'] : 0;
            // Check for stale lock
            if ($acquired_at > 0 && ($now - $acquired_at) > $timeout) {
                // Break stale lock
                delete_transient(self::SCHEMA_LOCK_KEY);
            } else {
                return false; // Active lock held
            }
        }

        $lock_payload = [
            'token' => $lock_token,
            'time'  => $now,
            'pid'   => function_exists('getmypid') ? getmypid() : 0
        ];
        set_transient(self::SCHEMA_LOCK_KEY, $lock_payload, $timeout);
        return true;
    }

    /**
     * Releases distributed lock if owned by the provided token.
     *
     * @param string $lock_token
     * @return bool True if lock was owned and deleted, false otherwise.
     */
    public static function release_lock(string $lock_token): bool {
        $lock = get_transient(self::SCHEMA_LOCK_KEY);
        if (!$lock) {
            return true;
        }

        if (is_array($lock) && isset($lock['token']) && $lock['token'] === $lock_token) {
            delete_transient(self::SCHEMA_LOCK_KEY);
            return true;
        }

        return false;
    }

    /**
     * Checkpoint registration for in-memory request lifecycle (CPTs, single-boot guard).
     * Prevents duplicate execution within the same request cycle.
     *
     * @param string $checkpoint_name
     * @return bool True if registered first time, false if already registered.
     */
    public static function register_checkpoint(string $checkpoint_name): bool {
        if (isset(self::$in_memory_checkpoints[$checkpoint_name])) {
            return false;
        }

        self::$in_memory_checkpoints[$checkpoint_name] = true;
        return true;
    }

    /**
     * Resets in-memory request checkpoints.
     *
     * @return void
     */
    public static function reset_checkpoints(): void {
        self::$in_memory_checkpoints = [];
    }
}
