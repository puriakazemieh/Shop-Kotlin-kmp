<?php
namespace Carmilla\Core\Lifecycle;

use Carmilla\Core\Migration\SchemaRunner;

/**
 * Lifecycle Hooks Manager.
 *
 * Coordinates lifecycle transitions (activation, deactivation, theme switch,
 * and uninstallation) across both Carmilla Plugin and Carmilla Theme.
 *
 * Guarantees data preservation unless an explicit, auditable opt-in for data deletion
 * (`carmilla_delete_data_on_uninstall`) has been enabled by an administrator.
 *
 * Task: P04-WPPLUGIN-CODE-012
 *
 * @package Carmilla\Core\Lifecycle
 */
class LifecycleManager {

	/**
	 * Option key controlling whether data should be wiped upon uninstallation.
	 */
	public const OPT_DELETE_ON_UNINSTALL = 'carmilla_delete_data_on_uninstall';

	/**
	 * In-memory registry of executed lifecycle checkpoints to avoid duplicate runs.
	 *
	 * @var array<string, bool>
	 */
	private static array $executed_checkpoints = array();

	/**
	 * Registers the activation hook handler for the specified plugin file.
	 *
	 * Executes schema migrations, flushes rewrite rules, and initializes core options.
	 *
	 * @param string $plugin_file Main plugin file path (__FILE__).
	 * @return void
	 */
	public static function register_activation_hook_handler( string $plugin_file ): void {
		register_activation_hook(
			$plugin_file,
			static function () {
				self::handle_activation();
			}
		);
	}

	/**
	 * Handles activation logic.
	 *
	 * Runs schema migrations and prepares rewrite rules.
	 *
	 * @return void
	 */
	public static function handle_activation(): void {
		if ( ! self::acquire_checkpoint( 'activation' ) ) {
			return;
		}

		// 1. Run core schema migrations.
		if ( class_exists( '\Carmilla\Core\Migration\SchemaRunner' ) ) {
			SchemaRunner::run_migrations();
		}

		// 2. Register core CPTs and metadata if needed prior to rewrite flush.
		if ( class_exists( 'CB_CPT' ) ) {
			\CB_CPT::register();
		}

		// 3. Flush rewrite rules (soft flush).
		flush_rewrite_rules( false );

		// 4. Record activation tracking options.
		update_option( 'carmilla_activated_at', time() );
		update_option( 'carmilla_lifecycle_status', 'active' );

		// 5. Ensure deletion on uninstall defaults to false (strict data retention policy).
		if ( false === get_option( self::OPT_DELETE_ON_UNINSTALL, false ) ) {
			add_option( self::OPT_DELETE_ON_UNINSTALL, false );
		}
	}

	/**
	 * Registers the deactivation hook handler for the specified plugin file.
	 *
	 * Flushes rewrite rules, purges ephemeral transients, but PRESERVES all persistent data.
	 *
	 * @param string $plugin_file Main plugin file path (__FILE__).
	 * @return void
	 */
	public static function register_deactivation_hook_handler( string $plugin_file ): void {
		register_deactivation_hook(
			$plugin_file,
			static function () {
				self::handle_deactivation();
			}
		);
	}

	/**
	 * Handles deactivation logic.
	 *
	 * Cleans temporary caches and transients while preserving all custom posts,
	 * WooCommerce orders, appointments, psych tests, and settings.
	 *
	 * @return void
	 */
	public static function handle_deactivation(): void {
		if ( ! self::acquire_checkpoint( 'deactivation' ) ) {
			return;
		}

		// 1. Flush rewrite rules to clean up endpoints.
		flush_rewrite_rules( false );

		// 2. Clean temporary transients only (DO NOT TOUCH DATA).
		self::clean_transients();

		// 3. Reset boot checkpoints so subsequent boots can re-initialize properly.
		if ( class_exists( '\Carmilla\Core\Migration\SchemaRunner' ) ) {
			SchemaRunner::reset_checkpoints();
		}

		// 4. Update lifecycle tracking option.
		update_option( 'carmilla_deactivated_at', time() );
		update_option( 'carmilla_lifecycle_status', 'deactivated' );
	}

	/**
	 * Registers the theme switch handler.
	 *
	 * Handles graceful degradation when Carmilla Theme is switched to another theme.
	 * Preserves all database content, settings, and CPT records.
	 *
	 * @return void
	 */
	public static function register_theme_switch_handler(): void {
		add_action(
			'switch_theme',
			array( __CLASS__, 'handle_theme_switch' ),
			10,
			3
		);
	}

	/**
	 * Handles theme switch event.
	 *
	 * On theme switch, gracefully degrades presentation assets but keeps all data intact.
	 *
	 * @param string             $new_name  Name of the new theme.
	 * @param \WP_Theme|null     $new_theme WP_Theme object of the new theme.
	 * @param \WP_Theme|null     $old_theme WP_Theme object of the old theme.
	 * @return void
	 */
	public static function handle_theme_switch( string $new_name = '', $new_theme = null, $old_theme = null ): void {
		if ( ! self::acquire_checkpoint( 'theme_switch' ) ) {
			return;
		}

		// 1. Flush rewrite rules so archive and single endpoints adapt cleanly.
		flush_rewrite_rules( false );

		// 2. Clear theme-specific transient caches.
		delete_transient( 'carmilla_theme_nav_cache' );
		delete_transient( 'carmilla_theme_template_cache' );

		// 3. Reset kernel boot checkpoints for the next lifecycle cycle.
		if ( class_exists( '\Carmilla\Core\Migration\SchemaRunner' ) ) {
			SchemaRunner::reset_checkpoints();
		}

		// 4. Log the theme transition.
		update_option( 'carmilla_last_theme_switch', array(
			'switched_at' => time(),
			'new_theme'   => $new_name,
			'old_theme'   => is_object( $old_theme ) ? $old_theme->get( 'Name' ) : '',
		) );
	}

	/**
	 * Registers the uninstall handler.
	 *
	 * Data is deleted ONLY if explicit opt-in via `get_option('carmilla_delete_data_on_uninstall')`
	 * is enabled by an administrator.
	 *
	 * @param string|null $plugin_file Main plugin file path or null if calling directly.
	 * @return void
	 */
	public static function register_uninstall_handler( ?string $plugin_file = null ): void {
		if ( ! empty( $plugin_file ) ) {
			register_uninstall_hook(
				$plugin_file,
				array( __CLASS__, 'handle_uninstall' )
			);
		}
	}

	/**
	 * Handles uninstallation logic.
	 *
	 * Strictly respects data retention policies:
	 * If `carmilla_delete_data_on_uninstall` is not explicitly set to true,
	 * all content, settings, and tables are preserved.
	 *
	 * @return void
	 */
	public static function handle_uninstall(): void {
		if ( ! self::acquire_checkpoint( 'uninstall' ) ) {
			return;
		}

		$delete_data_opt_in = get_option( self::OPT_DELETE_ON_UNINSTALL, false );

		// Default behavior: PRESERVE ALL DATA unless explicit opt-in.
		if ( ! $delete_data_opt_in || '1' !== (string) $delete_data_opt_in ) {
			self::clean_transients();
			return;
		}

		// Explicit opt-in confirmed: Purge Carmilla options and settings.
		delete_option( 'carmilla_settings' );
		delete_option( 'carmilla_settings_audit_log' );
		delete_option( 'carmilla_schema_version' );
		delete_option( 'carmilla_boot_checkpoints' );
		delete_option( 'carmilla_core_installed' );
		delete_option( 'carmilla_activated_at' );
		delete_option( 'carmilla_deactivated_at' );
		delete_option( 'carmilla_lifecycle_status' );
		delete_option( 'carmilla_last_theme_switch' );
		delete_option( self::OPT_DELETE_ON_UNINSTALL );

		self::clean_transients();
	}

	/**
	 * Cleans all Carmilla-related transient caches.
	 *
	 * @return void
	 */
	public static function clean_transients(): void {
		delete_transient( 'carmilla_schema_lock' );
		delete_transient( 'carmilla_effective_features' );
		delete_transient( 'carmilla_boot_checkpoints' );
		delete_transient( 'carmilla_theme_nav_cache' );
		delete_transient( 'carmilla_theme_template_cache' );
	}

	/**
	 * Checks and acquires a kernel boot checkpoint to prevent duplicate execution.
	 *
	 * @param string $action Name of the lifecycle action.
	 * @return bool True if checkpoint was acquired (first execution), false otherwise.
	 */
	public static function acquire_checkpoint( string $action ): bool {
		$checkpoint_key = 'lifecycle_' . $action;

		// 1. Check in-memory guard.
		if ( isset( self::$executed_checkpoints[ $checkpoint_key ] ) ) {
			return false;
		}

		// 2. Check kernel boot checkpoint runner.
		if ( class_exists( '\Carmilla\Core\Migration\SchemaRunner' ) ) {
			$registered = SchemaRunner::register_checkpoint( $checkpoint_key );
			if ( ! $registered ) {
				return false;
			}
		}

		self::$executed_checkpoints[ $checkpoint_key ] = true;
		return true;
	}

	/**
	 * Resets in-memory checkpoint cache (primarily for automated testing).
	 *
	 * @return void
	 */
	public static function reset_memory_checkpoints(): void {
		self::$executed_checkpoints = array();
	}
}
