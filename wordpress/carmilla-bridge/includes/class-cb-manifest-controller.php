<?php
/**
 * Canonical, public client capability manifest. It deliberately exposes no
 * credential, private option or backend-origin override.
 */
if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Manifest_Controller {
	private const FEATURES_OPTION = 'cb_manifest_features';
	private const AUDIT_OPTION = 'cb_manifest_audit_log';

	public function register_admin_page(): void {
		add_options_page(
			__( 'Carmilla Feature Manifest', 'carmilla-bridge' ),
			__( 'Carmilla Manifest', 'carmilla-bridge' ),
			'manage_options',
			'cb-feature-manifest',
			array( $this, 'render_admin_page' )
		);
	}

	public function save_admin_settings(): void {
		if ( ! current_user_can( 'manage_options' ) ) {
			wp_die( esc_html__( 'You are not allowed to change the client manifest.', 'carmilla-bridge' ) );
		}
		check_admin_referer( 'cb_save_manifest_settings' );
		$submitted = isset( $_POST['cb_manifest_features'] ) && is_array( $_POST['cb_manifest_features'] )
			? $_POST['cb_manifest_features'] : array();
		$configured = array();
		foreach ( self::feature_defaults() as $id => $default ) {
			$configured[ $id ] = ! empty( $submitted[ $id ] );
		}
		$violations = self::dependency_violations( $configured );
		if ( ! empty( $violations ) ) {
			self::write_audit( 'manifest_update_rejected', array( 'violations' => $violations ) );
			wp_safe_redirect( add_query_arg( array(
				'page' => 'cb-feature-manifest',
				'cb_manifest_error' => implode( ', ', $violations ),
			), admin_url( 'options-general.php' ) ) );
			exit;
		}
		update_option( self::FEATURES_OPTION, $configured, false );
		update_option( 'cb_manifest_version', gmdate( 'Y.m.d.His' ), false );
		self::write_audit( 'manifest_updated', array( 'enabled' => array_keys( array_filter( $configured ) ) ) );
		wp_safe_redirect( add_query_arg( array(
			'page' => 'cb-feature-manifest',
			'cb_manifest_updated' => '1',
		), admin_url( 'options-general.php' ) ) );
		exit;
	}

	public function render_admin_page(): void {
		if ( ! current_user_can( 'manage_options' ) ) {
			return;
		}
		$configured = self::normalize_configured_features( get_option( self::FEATURES_OPTION, array() ) );
		?>
		<div class="wrap">
			<h1><?php echo esc_html__( 'Carmilla Feature Manifest', 'carmilla-bridge' ); ?></h1>
			<?php if ( isset( $_GET['cb_manifest_error'] ) ) : ?>
				<div class="notice notice-error"><p><?php echo esc_html( sprintf( __( 'Saved nothing. These features need an enabled parent: %s', 'carmilla-bridge' ), wp_unslash( $_GET['cb_manifest_error'] ) ) ); ?></p></div>
			<?php elseif ( isset( $_GET['cb_manifest_updated'] ) ) : ?>
				<div class="notice notice-success"><p><?php echo esc_html__( 'Manifest updated.', 'carmilla-bridge' ); ?></p></div>
			<?php endif; ?>
			<p><?php echo esc_html__( 'A child feature cannot be saved unless all of its required parent features are enabled. Every accepted or rejected save is recorded in the Carmilla manifest audit log.', 'carmilla-bridge' ); ?></p>
			<form method="post" action="<?php echo esc_url( admin_url( 'admin-post.php' ) ); ?>">
				<input type="hidden" name="action" value="cb_save_manifest_settings" />
				<?php wp_nonce_field( 'cb_save_manifest_settings' ); ?>
				<table class="form-table" role="presentation"><tbody>
				<?php foreach ( self::feature_defaults() as $id => $default ) : ?>
					<tr><th scope="row"><?php echo esc_html( $id ); ?></th><td><label>
						<input type="checkbox" name="cb_manifest_features[<?php echo esc_attr( $id ); ?>]" value="1" <?php checked( $configured[ $id ] ); ?> />
						<?php echo esc_html__( 'Enabled', 'carmilla-bridge' ); ?>
					</label></td></tr>
				<?php endforeach; ?>
				</tbody></table>
				<?php submit_button( __( 'Save manifest', 'carmilla-bridge' ) ); ?>
			</form>
		</div>
		<?php
	}

	public function register_routes(): void {
		register_rest_route( CB_REST_NAMESPACE, '/client-manifest', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'get_manifest' ),
			'permission_callback' => '__return_true',
		) );
	}

	/**
	 * Enforce the manifest policy at the REST dispatch boundary. Controllers may
	 * still expose routes for compatibility, but a disabled feature never reaches
	 * its callback. Authentication/manifest routes are intentionally unmapped.
	 */
	public static function guard_rest_request( $result, $server, $request ) {
		if ( $result !== null ) {
			return $result;
		}
		$feature = self::feature_for_route( $request->get_route() );
		if ( $feature === null ) {
			return $result;
		}
		$features = self::resolve_features( get_option( self::FEATURES_OPTION, array() ) );
		if ( ! empty( $features[ $feature ] ) ) {
			return $result;
		}
		return new WP_Error(
			'FEATURE_DISABLED',
			__( 'این قابلیت در حال حاضر فعال نیست.', 'carmilla-bridge' ),
			array( 'status' => 403, 'feature' => $feature )
		);
	}

	/** Map public REST paths to the canonical feature identifier. */
	public static function feature_for_route( $route ): ?string {
		$route = '/' . ltrim( (string) $route, '/' );
		$api_position = strpos( $route, '/api/' );
		if ( $api_position === false ) {
			return null;
		}
		$path = substr( $route, $api_position );
		$prefixes = array(
			'/api/admin/psych-tests' => 'psych.tests',
			'/api/psych-tests'       => 'psych.tests',
			'/api/admin/therapists'  => 'clinic.booking',
			'/api/therapists'        => 'clinic.booking',
			'/api/clinic'            => 'clinic.booking',
			'/api/admin/courses'     => 'academy.core',
			'/api/courses'           => 'academy.core',
			'/api/academy'           => 'academy.core',
			'/api/course-requests'   => 'academy.core',
			'/api/admin/blogs'       => 'content.blog',
			'/api/blogs'             => 'content.blog',
			'/api/admin/products'    => 'commerce.core',
			'/api/admin/variants'    => 'commerce.core',
			'/api/admin/options'     => 'commerce.core',
			'/api/admin/bundles'     => 'commerce.core',
			'/api/products'          => 'commerce.core',
			'/api/categories'        => 'commerce.core',
			'/api/campaigns'         => 'commerce.core',
			'/api/banners'           => 'commerce.core',
			'/api/cart'              => 'commerce.core',
			'/api/orders'            => 'commerce.core',
			'/api/payments'          => 'commerce.core',
			'/api/bundles'           => 'commerce.core',
			'/api/wallet'            => 'wallet',
			'/api/admin'             => 'admin.mobile',
		);
		foreach ( $prefixes as $prefix => $feature ) {
			if ( $path === $prefix || strpos( $path, $prefix . '/' ) === 0 ) {
				return $feature;
			}
		}
		return null;
	}

	public function get_manifest( WP_REST_Request $request ): WP_REST_Response {
		$payload = array(
			'schemaVersion'     => 1,
			'manifestVersion'   => (string) get_option( 'cb_manifest_version', '2026.08.1' ),
			'backendProfile'    => 'WORDPRESS',
			'tenantId'          => sanitize_key( (string) get_option( 'cb_manifest_tenant_id', get_current_blog_id() ) ),
			'minimumAppVersion' => (string) get_option( 'cb_manifest_min_client', '1.0.0' ),
			'issuedAt'          => gmdate( 'c' ),
			'expiresAt'         => gmdate( 'c', time() + DAY_IN_SECONDS ),
			'features'          => self::resolve_features( get_option( self::FEATURES_OPTION, array() ) ),
		);
		$etag = '"' . hash( 'sha256', wp_json_encode( $payload ) ) . '"';
		if ( $request->get_header( 'if-none-match' ) === $etag ) {
			$response = new WP_REST_Response( null, 304 );
			$response->header( 'ETag', $etag );
			return $response;
		}
		$response = new WP_REST_Response( $payload, 200 );
		$response->header( 'ETag', $etag );
		$response->header( 'Cache-Control', 'public, max-age=300' );
		return $response;
	}

	/** Visible for a dependency-free smoke test. */
	public static function resolve_features( $configured ): array {
		$defaults = self::normalize_configured_features( $configured );
		foreach ( self::feature_dependencies() as $child => $parents ) {
			foreach ( $parents as $parent ) {
				if ( empty( $defaults[ $parent ] ) ) { $defaults[ $child ] = false; break; }
			}
		}
		return $defaults;
	}

	/** Returns invalid child feature IDs without modifying the stored option. */
	public static function dependency_violations( $configured ): array {
		$configured = self::normalize_configured_features( $configured );
		$violations = array();
		foreach ( self::feature_dependencies() as $child => $parents ) {
			if ( empty( $configured[ $child ] ) ) {
				continue;
			}
			foreach ( $parents as $parent ) {
				if ( empty( $configured[ $parent ] ) ) {
					$violations[] = $child;
					break;
				}
			}
		}
		return $violations;
	}

	private static function feature_defaults(): array {
		return array(
			'content.blog' => true, 'commerce.core' => true, 'commerce.physical' => true,
			'commerce.digital' => false, 'academy.core' => false, 'academy.quiz' => false,
			'academy.certificate' => false, 'clinic.booking' => false, 'clinic.messaging' => false,
			'psych.tests' => false, 'wallet' => false, 'admin.mobile' => false,
		);
	}

	private static function feature_dependencies(): array {
		return array(
			'commerce.physical' => array( 'commerce.core' ), 'commerce.digital' => array( 'commerce.core' ),
			'academy.core' => array( 'content.blog' ), 'academy.quiz' => array( 'academy.core' ),
			'academy.certificate' => array( 'academy.core' ), 'clinic.booking' => array( 'content.blog' ),
			'clinic.messaging' => array( 'clinic.booking' ), 'psych.tests' => array( 'content.blog' ), 'wallet' => array( 'commerce.core' ),
		);
	}

	private static function normalize_configured_features( $configured ): array {
		$defaults = self::feature_defaults();
		$configured = is_array( $configured ) ? $configured : array();
		foreach ( $defaults as $id => $default ) {
			if ( array_key_exists( $id, $configured ) ) {
				$defaults[ $id ] = (bool) $configured[ $id ];
			}
		}
		return $defaults;
	}

	private static function write_audit( string $event, array $context ): void {
		$entries = get_option( self::AUDIT_OPTION, array() );
		$entries = is_array( $entries ) ? $entries : array();
		$entries[] = array(
			'event' => $event,
			'at' => gmdate( 'c' ),
			'userId' => get_current_user_id(),
			'context' => $context,
		);
		update_option( self::AUDIT_OPTION, array_slice( $entries, -50 ), false );
	}
}
