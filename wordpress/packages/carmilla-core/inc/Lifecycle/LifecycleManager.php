<?php
namespace Carmilla\Core\Lifecycle;

use Carmilla\Core\Capabilities\CapabilityRegistry;
use Carmilla\Core\Migration\SchemaRunner;

if ( ! class_exists( __NAMESPACE__ . '\LifecycleManager', false ) ) {

/**
 * Lifecycle Hooks Manager.
 *
 * Coordinates lifecycle transitions (activation, deactivation, theme switch,
 * and uninstallation) across both Carmilla Plugin and Carmilla Theme.
 *
 * Guarantees data preservation unless an explicit, auditable opt-in for data deletion
 * (`carmilla_delete_data_on_uninstall`) has been enabled by an administrator.
 * Full cross-host adoption and license migration are delegated to P04-WORDPRESS-DATA-042.
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
	 * Nonce action for explicit administrator data cleanup.
	 */
	public const CLEANUP_NONCE_ACTION = 'carmilla_explicit_data_cleanup';

	/**
	 * Cross-host adoption and license transfer task reference.
	 */
	public const ADOPTION_TASK_REF = 'P04-WORDPRESS-DATA-042';

	/**
	 * In-memory registry of executed lifecycle checkpoints to avoid duplicate runs.
	 *
	 * @var array<string, bool>
	 */
	private static array $executed_checkpoints = array();

	/**
	 * Registers the activation hook handler for the specified plugin file.
	 *
	 * @param string $plugin_file Main plugin file path (__FILE__).
	 * @return void
	 */
	public static function register_activation_hook_handler( string $plugin_file ): void {
		if ( function_exists( 'register_activation_hook' ) ) {
			register_activation_hook(
				$plugin_file,
				static function () {
					self::handle_activation();
				}
			);
		}
	}

	/**
	 * Handles activation logic.
	 *
	 * Runs schema migrations, prepares rewrite rules, and enforces default data retention.
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
		if ( function_exists( 'flush_rewrite_rules' ) ) {
			flush_rewrite_rules( false );
		}

		// 4. Record activation tracking options.
		if ( function_exists( 'update_option' ) ) {
			update_option( 'carmilla_activated_at', time() );
			update_option( 'carmilla_lifecycle_status', 'active' );
		}

		// 5. Ensure deletion on uninstall defaults to false (strict data retention policy).
		if ( function_exists( 'get_option' ) && function_exists( 'add_option' ) ) {
			if ( false === get_option( self::OPT_DELETE_ON_UNINSTALL, false ) ) {
				add_option( self::OPT_DELETE_ON_UNINSTALL, false );
			}
		}
	}

	/**
	 * Registers the deactivation hook handler for the specified plugin file.
	 *
	 * @param string $plugin_file Main plugin file path (__FILE__).
	 * @return void
	 */
	public static function register_deactivation_hook_handler( string $plugin_file ): void {
		if ( function_exists( 'register_deactivation_hook' ) ) {
			register_deactivation_hook(
				$plugin_file,
				static function () {
					self::handle_deactivation();
				}
			);
		}
	}

	/**
	 * Handles deactivation logic for the plugin host.
	 *
	 * Preserves all persistent data (posts, orders, appointments, settings).
	 * If Carmilla Theme remains active, services continue on the Theme host (`active_single_host`).
	 * If no compatible Carmilla host remains, explicitly records `halted_no_active_host`.
	 *
	 * @param bool|null $theme_active_override Optional override for testing.
	 * @return void
	 */
	public static function handle_deactivation( ?bool $theme_active_override = null ): void {
		if ( ! self::acquire_checkpoint( 'deactivation' ) ) {
			return;
		}

		if ( function_exists( 'flush_rewrite_rules' ) ) {
			flush_rewrite_rules( false );
		}

		// Clean temporary transients only (NEVER delete persistent site data).
		self::clean_transients();

		// Reset boot checkpoints so subsequent boots can re-initialize properly.
		if ( class_exists( '\Carmilla\Core\Migration\SchemaRunner' ) ) {
			SchemaRunner::reset_checkpoints();
		}

		$theme_still_active = $theme_active_override ?? self::is_carmilla_theme_active();
		$next_status = $theme_still_active ? 'active_single_host' : 'halted_no_active_host';

		if ( function_exists( 'update_option' ) ) {
			update_option( 'carmilla_deactivated_at', time() );
			update_option( 'carmilla_lifecycle_status', $next_status );
		}
	}

	/**
	 * Registers the theme switch handler.
	 *
	 * @return void
	 */
	public static function register_theme_switch_handler(): void {
		if ( function_exists( 'add_action' ) ) {
			add_action(
				'switch_theme',
				array( __CLASS__, 'handle_theme_switch' ),
				10,
				3
			);
		}
	}

	/**
	 * Handles theme switch event.
	 *
	 * Preserves all database content, settings, and CPT records.
	 * If switching away from Carmilla Theme while Bridge Plugin is inactive,
	 * explicitly marks lifecycle status as `halted_no_active_host`.
	 *
	 * @param string         $new_name               Name of the new theme.
	 * @param \WP_Theme|null $new_theme              WP_Theme object of the new theme.
	 * @param \WP_Theme|null $old_theme              WP_Theme object of the old theme.
	 * @param bool|null      $plugin_active_override Optional override for testing.
	 * @return void
	 */
	public static function handle_theme_switch(
		string $new_name = '',
		$new_theme = null,
		$old_theme = null,
		?bool $plugin_active_override = null
	): void {
		if ( ! self::acquire_checkpoint( 'theme_switch' ) ) {
			return;
		}

		if ( function_exists( 'flush_rewrite_rules' ) ) {
			flush_rewrite_rules( false );
		}

		// Clear theme-specific transient caches only (NEVER delete persistent data).
		if ( function_exists( 'delete_transient' ) ) {
			delete_transient( 'carmilla_theme_nav_cache' );
			delete_transient( 'carmilla_theme_template_cache' );
		}

		if ( class_exists( '\Carmilla\Core\Migration\SchemaRunner' ) ) {
			SchemaRunner::reset_checkpoints();
		}

		$new_stylesheet = is_object( $new_theme ) && method_exists( $new_theme, 'get_stylesheet' )
			? strtolower( (string) $new_theme->get_stylesheet() )
			: strtolower( $new_name );
		$is_new_carmilla = ( false !== strpos( $new_stylesheet, 'carmilla' ) );
		$plugin_active   = $plugin_active_override ?? self::is_bridge_plugin_active();

		if ( $is_new_carmilla ) {
			$next_status = $plugin_active ? 'active' : 'active_single_host';
		} else {
			$next_status = $plugin_active ? 'active_single_host' : 'halted_no_active_host';
		}

		if ( function_exists( 'update_option' ) ) {
			update_option( 'carmilla_lifecycle_status', $next_status );
			update_option(
				'carmilla_last_theme_switch',
				array(
					'switched_at' => time(),
					'new_theme'   => $new_name,
					'old_theme'   => ( is_object( $old_theme ) && method_exists( $old_theme, 'get' ) ) ? $old_theme->get( 'Name' ) : '',
					'status'      => $next_status,
				)
			);
		}
	}

	/**
	 * Registers the uninstall handler.
	 *
	 * @param string|null $plugin_file Main plugin file path or null if calling directly.
	 * @return void
	 */
	public static function register_uninstall_handler( ?string $plugin_file = null ): void {
		if ( ! empty( $plugin_file ) && function_exists( 'register_uninstall_hook' ) ) {
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
	 * If `carmilla_delete_data_on_uninstall` is not explicitly set to true/1,
	 * all content, settings, and tables are preserved.
	 *
	 * @return void
	 */
	public static function handle_uninstall(): void {
		if ( ! self::acquire_checkpoint( 'uninstall' ) ) {
			return;
		}

		$delete_data_opt_in = function_exists( 'get_option' ) ? get_option( self::OPT_DELETE_ON_UNINSTALL, false ) : false;

		// Default behavior: PRESERVE ALL DATA unless explicit opt-in.
		if ( ! $delete_data_opt_in || '1' !== (string) $delete_data_opt_in ) {
			self::clean_transients();
			return;
		}

		self::purge_carmilla_options();
		self::clean_transients();
	}

	/**
	 * Executes explicit, administrator-initiated data cleanup.
	 * Requires:
	 * 1. Admin delete capability (`manage_options` / `carmilla_reset_system`)
	 * 2. Valid WordPress nonce (`CLEANUP_NONCE_ACTION`)
	 * 3. Explicit boolean opt-in (`$explicit_opt_in === true`)
	 *
	 * @param int    $user_id         Acting WordPress user ID.
	 * @param string $nonce           WordPress nonce string.
	 * @param bool   $explicit_opt_in Whether the admin explicitly checked opt-in confirmation.
	 * @return array{ok: bool, code: string, status: int, message: string}
	 */
	public static function execute_explicit_cleanup( int $user_id, string $nonce, bool $explicit_opt_in ): array {
		// 1. Authorization check
		$can_delete = class_exists( CapabilityRegistry::class )
			? CapabilityRegistry::user_can_for_domain( $user_id, 'admin', 'delete' )
			: ( function_exists( 'user_can' ) && user_can( $user_id, 'manage_options' ) );

		if ( ! $can_delete ) {
			return array(
				'ok'      => false,
				'code'    => 'FORBIDDEN',
				'status'  => 403,
				'message' => 'شما مجوز پاک‌سازی داده‌های سایت را ندارید.',
			);
		}

		// 2. Nonce / CSRF check
		$nonce_valid = function_exists( 'wp_verify_nonce' ) && (bool) wp_verify_nonce( $nonce, self::CLEANUP_NONCE_ACTION );
		if ( ! $nonce_valid ) {
			return array(
				'ok'      => false,
				'code'    => 'INVALID_NONCE',
				'status'  => 403,
				'message' => 'توکن امنیتی (Nonce) نامعتبر یا منقضی است.',
			);
		}

		// 3. Explicit opt-in check
		if ( ! $explicit_opt_in ) {
			return array(
				'ok'      => false,
				'code'    => 'OPT_IN_REQUIRED',
				'status'  => 400,
				'message' => 'برای پاک‌سازی داده‌ها باید تأییدیه صریح (opt-in) فعال شود.',
			);
		}

		self::purge_carmilla_options();
		self::clean_transients();

		return array(
			'ok'      => true,
			'code'    => 'CLEANUP_EXECUTED',
			'status'  => 200,
			'message' => 'پاک‌سازی صریح تنظیمات و کش‌های کارمیلا با موفقیت انجام شد.',
		);
	}

	/**
	 * Registers admin_post handler for explicit cleanup form submission.
	 *
	 * @return void
	 */
	public static function register_admin_cleanup_handler(): void {
		if ( function_exists( 'add_action' ) ) {
			add_action( 'admin_post_carmilla_explicit_cleanup', array( __CLASS__, 'handle_admin_cleanup_post' ) );
		}
	}

	/**
	 * Processes admin_post_carmilla_explicit_cleanup requests.
	 *
	 * @return void
	 */
	public static function handle_admin_cleanup_post(): void {
		$user_id = function_exists( 'get_current_user_id' ) ? get_current_user_id() : 0;
		$nonce   = isset( $_POST['_wpnonce'] ) ? sanitize_text_field( wp_unslash( $_POST['_wpnonce'] ) ) : '';
		$opt_in  = ! empty( $_POST['carmilla_confirm_cleanup'] ) && '1' === (string) $_POST['carmilla_confirm_cleanup'];

		$result = self::execute_explicit_cleanup( $user_id, $nonce, $opt_in );

		if ( ! $result['ok'] && function_exists( 'wp_die' ) ) {
			wp_die( esc_html( $result['message'] ), esc_html( $result['code'] ), array( 'response' => $result['status'] ) );
		}

		if ( function_exists( 'wp_safe_redirect' ) && function_exists( 'admin_url' ) ) {
			wp_safe_redirect( add_query_arg( 'carmilla_cleanup', 'done', admin_url( 'admin.php?page=carmilla-settings' ) ) );
			exit;
		}
	}

	/**
	 * Returns current dual-host lifecycle status and service execution state.
	 *
	 * @param bool|null $theme_active_override  Optional override for testing.
	 * @param bool|null $plugin_active_override Optional override for testing.
	 * @return array<string, mixed>
	 */
	public static function get_host_lifecycle_status( ?bool $theme_active_override = null, ?bool $plugin_active_override = null ): array {
		$theme_active  = $theme_active_override ?? self::is_carmilla_theme_active();
		$plugin_active = $plugin_active_override ?? self::is_bridge_plugin_active();

		$active_hosts = array();
		if ( $theme_active ) {
			$active_hosts[] = 'theme';
		}
		if ( $plugin_active ) {
			$active_hosts[] = 'plugin';
		}

		$services_running = ! empty( $active_hosts );
		if ( count( $active_hosts ) === 2 ) {
			$execution_status = 'active';
		} elseif ( count( $active_hosts ) === 1 ) {
			$execution_status = 'active_single_host';
		} else {
			$execution_status = 'halted_no_active_host';
		}

		return array(
			'theme_active'       => $theme_active,
			'plugin_active'      => $plugin_active,
			'active_hosts'       => $active_hosts,
			'services_running'   => $services_running,
			'execution_status'   => $execution_status,
			'persisted_status'   => function_exists( 'get_option' ) ? get_option( 'carmilla_lifecycle_status', $execution_status ) : $execution_status,
			'delete_on_uninstall'=> function_exists( 'get_option' ) ? (bool) get_option( self::OPT_DELETE_ON_UNINSTALL, false ) : false,
			'data_preserved'     => true,
			'adoption_task_ref'  => self::ADOPTION_TASK_REF,
		);
	}

	/**
	 * Purges Carmilla-owned options (called ONLY after explicit opt-in verification).
	 */
	private static function purge_carmilla_options(): void {
		if ( ! function_exists( 'delete_option' ) ) {
			return;
		}
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
	}

	/**
	 * Cleans all Carmilla-related transient caches.
	 *
	 * @return void
	 */
	public static function clean_transients(): void {
		if ( ! function_exists( 'delete_transient' ) ) {
			return;
		}
		delete_transient( 'carmilla_schema_lock' );
		delete_transient( 'carmilla_effective_features' );
		delete_transient( 'carmilla_boot_checkpoints' );
		delete_transient( 'carmilla_theme_nav_cache' );
		delete_transient( 'carmilla_theme_template_cache' );
	}

	/**
	 * Checks whether Carmilla Theme (or child theme) is currently active.
	 */
	private static function is_carmilla_theme_active(): bool {
		if ( ! function_exists( 'wp_get_theme' ) ) {
			return false;
		}
		$theme      = wp_get_theme();
		$stylesheet = strtolower( (string) $theme->get_stylesheet() );
		$template   = strtolower( (string) $theme->get_template() );
		return ( false !== strpos( $stylesheet, 'carmilla' ) || false !== strpos( $template, 'carmilla' ) );
	}

	/**
	 * Checks whether Carmilla Bridge plugin is currently active.
	 */
	private static function is_bridge_plugin_active(): bool {
		if ( ! function_exists( 'is_plugin_active' ) ) {
			return class_exists( 'CB_Plugin' );
		}
		return is_plugin_active( 'carmilla-bridge/carmilla-bridge.php' );
	}

	/**
	 * Checks and acquires a kernel boot checkpoint to prevent duplicate execution.
	 *
	 * @param string $action Name of the lifecycle action.
	 * @return bool True if checkpoint was acquired (first execution), false otherwise.
	 */
	public static function acquire_checkpoint( string $action ): bool {
		$checkpoint_key = 'lifecycle_' . $action;

		if ( isset( self::$executed_checkpoints[ $checkpoint_key ] ) ) {
			return false;
		}

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
		if ( class_exists( '\Carmilla\Core\Migration\SchemaRunner' ) ) {
			SchemaRunner::reset_checkpoints();
		}
	}
}

}
