<?php
/**
 * Dedicated Acceptance Test Suite for Task P04-WPPLUGIN-CODE-003:
 * 1. Clean installation from 0.0.0 to current schema version
 * 2. Incompatible downgrade prevention (fail closed, no writes)
 * 3. Lock acquisition, concurrency protection, and stale lock recovery
 * 4. Resumable step-level checkpoints after mid-migration failure (zero duplicate records)
 * 5. Co-install idempotency across Theme and Plugin boots
 *
 * Can be executed via Docker:
 * docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/packages/carmilla-core/tests/test-schema-runner-003.php
 */

if (!defined('ABSPATH')) {
    define('ABSPATH', __DIR__ . '/');
}

// In-memory options, transients, and DB store for mock testing
class Carmilla_Mock_WPDB {
    public $prefix = 'wp_';
    public $tables = [];
    public $rows = [];
    public $queries = [];

    public function get_charset_collate(): string {
        return 'DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci';
    }

    public function query($sql): bool {
        $this->queries[] = $sql;
        if (preg_match('/CREATE TABLE (?:IF NOT EXISTS )?`?([a-zA-Z0-9_]+)`?/i', $sql, $m)) {
            $table_name = $m[1];
            if (!isset($this->tables[$table_name])) {
                $this->tables[$table_name] = true;
                $this->rows[$table_name] = [];
            }
        }
        return true;
    }

    public function insert($table, $data): bool {
        $this->queries[] = "INSERT INTO {$table} " . json_encode($data);
        if (!isset($this->rows[$table])) {
            $this->rows[$table] = [];
        }
        $this->rows[$table][] = $data;
        return true;
    }

    public function get_count($table, $key = null, $val = null): int {
        if (!isset($this->rows[$table])) {
            return 0;
        }
        if ($key === null) {
            return count($this->rows[$table]);
        }
        $c = 0;
        foreach ($this->rows[$table] as $row) {
            if (isset($row[$key]) && $row[$key] === $val) {
                $c++;
            }
        }
        return $c;
    }

    public function reset(): void {
        $this->tables = [];
        $this->rows = [];
        $this->queries = [];
    }
}

$GLOBALS['mock_options'] = [];
$GLOBALS['mock_transients'] = [];
$GLOBALS['wpdb'] = new Carmilla_Mock_WPDB();

if (!function_exists('get_option')) {
    function get_option($name, $default = false) {
        return array_key_exists($name, $GLOBALS['mock_options']) ? $GLOBALS['mock_options'][$name] : $default;
    }
}
if (!function_exists('update_option')) {
    function update_option($name, $value, $autoload = null) {
        $GLOBALS['mock_options'][$name] = $value;
        return true;
    }
}
if (!function_exists('add_option')) {
    function add_option($name, $value) {
        if (!array_key_exists($name, $GLOBALS['mock_options'])) {
            $GLOBALS['mock_options'][$name] = $value;
            return true;
        }
        return false;
    }
}
if (!function_exists('delete_option')) {
    function delete_option($name) {
        unset($GLOBALS['mock_options'][$name]);
        return true;
    }
}
if (!function_exists('get_transient')) {
    function get_transient($name) {
        if (!isset($GLOBALS['mock_transients'][$name])) {
            return false;
        }
        $t = $GLOBALS['mock_transients'][$name];
        if (time() > $t['expires']) {
            unset($GLOBALS['mock_transients'][$name]);
            return false;
        }
        return $t['value'];
    }
}
if (!function_exists('set_transient')) {
    function set_transient($name, $value, $expiration = 0) {
        $GLOBALS['mock_transients'][$name] = [
            'value'   => $value,
            'expires' => time() + ($expiration > 0 ? $expiration : 86400)
        ];
        return true;
    }
}
if (!function_exists('delete_transient')) {
    function delete_transient($name) {
        unset($GLOBALS['mock_transients'][$name]);
        return true;
    }
}

// Load Kernel and SchemaRunner
require_once dirname(__DIR__) . '/Carmilla_Kernel.php';

use Carmilla\Core\Migration\SchemaRunner;

$fail = 0;
function assert_test(bool $condition, string $description) {
    global $fail;
    echo ($condition ? 'PASS' : 'FAIL') . " - " . $description . "\n";
    if (!$condition) {
        $fail++;
    }
}

echo "=================================================================\n";
echo " Running Acceptance Tests for P04-WPPLUGIN-CODE-003\n";
echo "=================================================================\n\n";

// -------------------------------------------------------------
// Test Group 1: Clean Installation from 0.0.0
// -------------------------------------------------------------
echo "-- Group 1: Clean Installation --\n";

$GLOBALS['mock_options'] = [];
$GLOBALS['mock_transients'] = [];
$GLOBALS['wpdb']->reset();
SchemaRunner::reset_checkpoints();

assert_test(SchemaRunner::get_current_schema_version() === '0.0.0', 'Initial schema version is 0.0.0');

$res_clean = SchemaRunner::run_migrations(['force_run' => true]);
assert_test($res_clean['success'] === true, 'Clean migration run succeeds');
assert_test($res_clean['status'] === 'migrated', 'Clean run status is migrated');
assert_test($res_clean['to'] === SchemaRunner::CURRENT_SCHEMA_VERSION, 'Target schema version reached (1.0.3)');
assert_test(SchemaRunner::get_current_schema_version() === '1.0.3', 'Option carmilla_schema_version is 1.0.3');

// Check that synthetic table and rows were created
assert_test(isset($GLOBALS['wpdb']->tables['wp_carmilla_synthetic_test']), 'Synthetic test table was created');
assert_test($GLOBALS['wpdb']->get_count('wp_carmilla_synthetic_test', 'item_key', 'synthetic_alpha') === 1, 'Initial seed Alpha row inserted');
assert_test($GLOBALS['wpdb']->get_count('wp_carmilla_synthetic_test', 'item_key', 'synthetic_beta') === 1, 'Initial seed Beta row inserted');

// -------------------------------------------------------------
// Test Group 2: Idempotent Re-execution (Noop)
// -------------------------------------------------------------
echo "\n-- Group 2: Idempotent Re-execution (Already Up-to-Date) --\n";

SchemaRunner::reset_checkpoints();
$res_noop = SchemaRunner::run_migrations(['force_run' => true]);
assert_test($res_noop['success'] === true, 'Re-run on up-to-date schema returns success');
assert_test($res_noop['status'] === 'noop', 'Status is noop');
assert_test($res_noop['reason'] === 'already_up_to_date', 'Reason is already_up_to_date');

// Ensure no duplicate rows were added
assert_test($GLOBALS['wpdb']->get_count('wp_carmilla_synthetic_test') === 2, 'Total rows in synthetic table remains exactly 2');

// -------------------------------------------------------------
// Test Group 3: Co-Install Single Execution in Same Request
// -------------------------------------------------------------
echo "\n-- Group 3: Co-Install Single Execution in Same Request --\n";

SchemaRunner::reset_checkpoints();
$res_first_host = SchemaRunner::run_migrations();
assert_test($res_first_host['status'] === 'noop' || $res_first_host['status'] === 'migrated', 'First host execution runs or checks status');

// Second host boots in same request (without force_run)
$res_second_host = SchemaRunner::run_migrations();
assert_test($res_second_host['success'] === true, 'Second host call succeeds');
assert_test($res_second_host['status'] === 'skipped', 'Second host call is skipped in same request');
assert_test($res_second_host['reason'] === 'already_run_in_request', 'Skip reason is already_run_in_request');

// -------------------------------------------------------------
// Test Group 4: Incompatible Downgrade Rejection
// -------------------------------------------------------------
echo "\n-- Group 4: Incompatible Downgrade Rejection --\n";

// Simulate database being on future schema version 2.0.0
update_option(SchemaRunner::SCHEMA_VERSION_KEY, '2.0.0');
SchemaRunner::reset_checkpoints();

$res_downgrade = SchemaRunner::run_migrations(['force_run' => true, 'target_version' => '1.0.3']);
assert_test($res_downgrade['success'] === false, 'Downgrade attempt returns success=false');
assert_test($res_downgrade['status'] === 'rejected', 'Downgrade status is rejected');
assert_test($res_downgrade['error'] === 'incompatible_downgrade_rejected', 'Error code is incompatible_downgrade_rejected');
assert_test(SchemaRunner::get_current_schema_version() === '2.0.0', 'Schema version option untouched at 2.0.0 (fail closed, no writes)');

// -------------------------------------------------------------
// Test Group 5: Concurrency Locking and Stale Lock Recovery
// -------------------------------------------------------------
echo "\n-- Group 5: Distributed Lock & Stale Lock Recovery --\n";

update_option(SchemaRunner::SCHEMA_VERSION_KEY, '1.0.0');
SchemaRunner::reset_checkpoints();

// Simulate active lock held by another process (acquired 10 seconds ago)
$active_lock = [
    'token' => 'external_proc_123',
    'time'  => time() - 10,
    'pid'   => 99999
];
set_transient(SchemaRunner::SCHEMA_LOCK_KEY, $active_lock, 60);

$res_locked = SchemaRunner::run_migrations(['force_run' => true]);
assert_test($res_locked['success'] === false, 'Migration locked by active lock is rejected');
assert_test($res_locked['status'] === 'locked', 'Status is locked');
assert_test($res_locked['error'] === 'migration_in_progress', 'Error is migration_in_progress');

// Simulate STALE lock (acquired 80 seconds ago, timeout is 60s)
$stale_lock = [
    'token' => 'dead_proc_777',
    'time'  => time() - 80,
    'pid'   => 88888
];
set_transient(SchemaRunner::SCHEMA_LOCK_KEY, $stale_lock, 60);

$res_stale_recovered = SchemaRunner::run_migrations(['force_run' => true]);
assert_test($res_stale_recovered['success'] === true, 'Stale lock is broken and migration succeeds');
assert_test($res_stale_recovered['status'] === 'migrated', 'Status is migrated after breaking stale lock');
assert_test(get_transient(SchemaRunner::SCHEMA_LOCK_KEY) === false, 'Lock is cleanly released after completion');

// -------------------------------------------------------------
// Test Group 6: Resumable Migration after Mid-Step Fault
// -------------------------------------------------------------
echo "\n-- Group 6: Resumable Migration & Zero Duplicate Data --\n";

// Reset to version 1.0.2 to test 1.0.3 synthetic migration step-by-step
$GLOBALS['mock_options'] = [];
$GLOBALS['mock_transients'] = [];
$GLOBALS['wpdb']->reset();
SchemaRunner::reset_checkpoints();
SchemaRunner::clear_persisted_checkpoints();
update_option(SchemaRunner::SCHEMA_VERSION_KEY, '1.0.2');

// Inject artificial fault at Step 3 (1.0.3_insert_seed_beta)
SchemaRunner::set_fault_injector(function(string $step_key) {
    if ($step_key === '1.0.3_insert_seed_beta') {
        throw new \RuntimeException("Artificially injected mid-migration fault at {$step_key}");
    }
});

// Run 1: Should execute step 1 and step 2, then fail at step 3
$res_failed_run = SchemaRunner::run_migrations(['force_run' => true]);
assert_test($res_failed_run['success'] === false, 'Run 1 fails due to injected fault');
assert_test($res_failed_run['status'] === 'failed', 'Run 1 status is failed');
assert_test(strpos($res_failed_run['error'], 'Artificially injected') !== false, 'Failure error caught properly');

// Verify state after failure:
assert_test(SchemaRunner::get_current_schema_version() === '1.0.2', 'Schema version remained at 1.0.2');
assert_test(SchemaRunner::is_step_completed('1.0.3_create_synthetic_table') === true, 'Step 1 checkpoint recorded');
assert_test(SchemaRunner::is_step_completed('1.0.3_insert_seed_alpha') === true, 'Step 2 checkpoint recorded');
assert_test(SchemaRunner::is_step_completed('1.0.3_insert_seed_beta') === false, 'Step 3 checkpoint NOT recorded');
assert_test($GLOBALS['wpdb']->get_count('wp_carmilla_synthetic_test', 'item_key', 'synthetic_alpha') === 1, 'Only Alpha row exists in DB');
assert_test($GLOBALS['wpdb']->get_count('wp_carmilla_synthetic_test', 'item_key', 'synthetic_beta') === 0, 'Beta row was NOT inserted');
assert_test(get_transient(SchemaRunner::SCHEMA_LOCK_KEY) === false, 'Lock was cleanly released in finally block');

// Run 2: Clear fault injector and RETRY
SchemaRunner::set_fault_injector(null);
SchemaRunner::reset_checkpoints();

$res_resumed_run = SchemaRunner::run_migrations(['force_run' => true]);
assert_test($res_resumed_run['success'] === true, 'Run 2 (retry) succeeds');
assert_test($res_resumed_run['status'] === 'migrated', 'Run 2 status is migrated');
assert_test($res_resumed_run['to'] === '1.0.3', 'Target version 1.0.3 reached');
assert_test(SchemaRunner::get_current_schema_version() === '1.0.3', 'Option carmilla_schema_version updated to 1.0.3');

// VERIFY ZERO DUPLICATE DATA:
$alpha_count = $GLOBALS['wpdb']->get_count('wp_carmilla_synthetic_test', 'item_key', 'synthetic_alpha');
$beta_count  = $GLOBALS['wpdb']->get_count('wp_carmilla_synthetic_test', 'item_key', 'synthetic_beta');
$total_count = $GLOBALS['wpdb']->get_count('wp_carmilla_synthetic_test');

assert_test($alpha_count === 1, 'Alpha row count is exactly 1 (NOT duplicated)');
assert_test($beta_count === 1, 'Beta row count is exactly 1');
assert_test($total_count === 2, 'Total row count in synthetic table is exactly 2');

echo "\n=================================================================\n";
echo ($fail === 0 ? "ALL P04-WPPLUGIN-CODE-003 ACCEPTANCE TESTS PASSED!\n" : "$fail TESTS FAILED!\n");
echo "=================================================================\n";

exit($fail === 0 ? 0 : 1);
