<?php
namespace Carmilla\Core\Rest;

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

require_once __DIR__ . '/JwtService.php';

/**
 * Unified REST Infrastructure
 *
 * Exposes standardized REST registration, structured envelopes for responses and errors,
 * pagination headers, nonce/param validation, legacy alias routing, and Bearer JWT auth
 * across Carmilla Theme, Bridge Plugin, and KMP clients.
 *
 * Strictly adheres to two backend profiles: 'WORDPRESS' and 'SPRING' (no third backend profile).
 * Diagnostic provider mode ('theme', 'plugin', or 'co-install') is communicated via provider metadata.
 *
 * Part of Task P04-WPPLUGIN-CODE-009.
 */
if ( ! class_exists( 'Carmilla\Core\Rest\RestInfrastructure', false ) ) {
class RestInfrastructure {

    /**
     * Canonical REST API namespace.
     *
     * @var string
     */
    public const NAMESPACE = 'carmilla/v1';

    /**
     * Default maximum per_page value for pagination safety.
     *
     * @var int
     */
    public const MAX_PER_PAGE = 100;

    /**
     * Track if infrastructure hooks have been initialized.
     *
     * @var bool
     */
    private static bool $initialized = false;

    /**
     * Initialize REST infrastructure hooks.
     */
    public static function init(): void {
        if ( self::$initialized ) {
            return;
        }
        self::$initialized = true;

        // Register routes and manifest
        add_action( 'rest_api_init', [ self::class, 'on_rest_api_init' ], 10 );

        // Enforce feature gate policy at dispatch boundary
        add_filter( 'rest_request_before_callbacks', [ self::class, 'guard_rest_request' ], 10, 3 );

        // Add provider diagnostic headers
        add_filter( 'rest_post_dispatch', [ self::class, 'add_provider_headers' ], 10, 3 );

        // Handle legacy root aliases (/api/users/me, /api/addresses, etc.)
        add_action( 'parse_request', [ self::class, 'handle_root_aliases' ], 5 );

        // Bearer token authentication filter
        add_filter( 'determine_current_user', [ self::class, 'authenticate_bearer' ], 20 );
    }

    /**
     * Registers canonical routes on rest_api_init if not already registered.
     */
    public static function on_rest_api_init(): void {
        // Check if route is already registered to maintain singular endpoint registration
        $server = function_exists( 'rest_get_server' ) ? rest_get_server() : null;
        if ( $server && is_object( $server ) ) {
            $routes = $server->get_routes();
            if ( ! empty( $routes[ '/' . self::NAMESPACE . '/client-manifest' ] ) ) {
                return;
            }
        }

        // Register canonical manifest endpoint
        self::register_route( '/client-manifest', [
            'methods'             => 'GET',
            'callback'            => [ self::class, 'get_manifest_response' ],
            'permission_callback' => '__return_true',
        ], false );

        // Register SKU-aware preflight diagnostics endpoints
        if ( class_exists( '\Carmilla\Core\Health\HealthChecker' ) ) {
            \Carmilla\Core\Health\HealthChecker::register_rest_routes();
        }

        // Register shared operational settings endpoints (P04-WPPLUGIN-CODE-014)
        if ( class_exists( '\Carmilla\Core\Settings\SettingsService' ) ) {
            \Carmilla\Core\Settings\SettingsService::register_rest_routes();
        }

        // Register shared entitlement toggle panel endpoints (P04-ENTITLEMENT-CODE-041)
        if ( class_exists( '\Carmilla\Core\Entitlement\FeatureToggleService' ) ) {
            \Carmilla\Core\Entitlement\FeatureToggleService::register_rest_routes();
        }

        // Register shared content (posts, pages, media) endpoints (P04-WORDPRESS-CODE-026)
        if ( class_exists( '\Carmilla\Core\Content\ContentRestController' ) ) {
            \Carmilla\Core\Content\ContentRestController::register_routes();
        }

        // Register shared catalog & product endpoints (P04-WORDPRESS-CODE-026A)
        if ( class_exists( '\Carmilla\Core\Catalog\CatalogRestController' ) ) {
            \Carmilla\Core\Catalog\CatalogRestController::register_routes();
        }

        // Register shared cart & order endpoints (P04-WORDPRESS-CODE-026B)
        if ( class_exists( '\Carmilla\Core\Commerce\CartOrderRestController' ) ) {
            \Carmilla\Core\Commerce\CartOrderRestController::register_routes();
        }

        // Register shared academy course & lesson catalog endpoints (P04-WORDPRESS-CODE-027)
        if ( class_exists( '\Carmilla\Core\Academy\CourseCatalogRestController' ) ) {
            \Carmilla\Core\Academy\CourseCatalogRestController::register_routes();
        }

        // Register shared academy enrollment & progress endpoints (P04-WORDPRESS-CODE-027A)
        if ( class_exists( '\Carmilla\Core\Academy\CourseEnrollmentRestController' ) ) {
            \Carmilla\Core\Academy\CourseEnrollmentRestController::register_routes();
        }

        // Register shared academy quiz & certificate endpoints (P04-WORDPRESS-CODE-027B)
        if ( class_exists( '\Carmilla\Core\Academy\CourseQuizRestController' ) ) {
            \Carmilla\Core\Academy\CourseQuizRestController::register_routes();
        }

        // Register shared therapist catalog & availability endpoints (P04-WORDPRESS-CODE-028)
        if ( class_exists( '\Carmilla\Core\Clinic\TherapistCatalogRestController' ) ) {
            \Carmilla\Core\Clinic\TherapistCatalogRestController::register_routes();
        }

        // Register shared appointment booking, cancel & management endpoints (P04-WORDPRESS-CODE-028A)
        if ( class_exists( '\Carmilla\Core\Clinic\AppointmentBookingRestController' ) ) {
            \Carmilla\Core\Clinic\AppointmentBookingRestController::register_routes();
        }

        // Register shared psychological test endpoints (P04-WORDPRESS-CODE-029)
        if ( class_exists( '\Carmilla\Core\Psych\PsychTestRestController' ) ) {
            \Carmilla\Core\Psych\PsychTestRestController::register_routes();
        }
    }

    /**
     * Resolves the current active provider mode: 'theme', 'plugin', or 'co-install'.
     *
     * @return string
     */
    public static function get_provider_mode(): string {
        $has_plugin = false;
        if ( function_exists( 'is_plugin_active' ) ) {
            $has_plugin = is_plugin_active( 'carmilla-bridge/carmilla-bridge.php' );
        } elseif ( function_exists( 'get_option' ) ) {
            $active_plugins = (array) get_option( 'active_plugins', [] );
            $has_plugin     = in_array( 'carmilla-bridge/carmilla-bridge.php', $active_plugins, true );
        } elseif ( class_exists( '\Carmilla_Kernel' ) ) {
            $has_plugin = \Carmilla_Kernel::is_booted_by( 'plugin' );
        }

        $is_carmilla_theme = false;
        if ( function_exists( 'wp_get_theme' ) ) {
            $theme = wp_get_theme();
            $sheet = strtolower( (string) $theme->get_stylesheet() );
            $tpl   = strtolower( (string) $theme->get_template() );
            $is_carmilla_theme = ( 'carmilla-theme' === $sheet || 'carmilla-theme' === $tpl || false !== strpos( $sheet, 'carmilla' ) );
        } elseif ( class_exists( '\Carmilla_Kernel' ) ) {
            $is_carmilla_theme = \Carmilla_Kernel::is_booted_by( 'theme' );
        }

        if ( $has_plugin && $is_carmilla_theme ) {
            return 'co-install';
        }
        if ( $has_plugin ) {
            return 'plugin';
        }
        if ( $is_carmilla_theme ) {
            return 'theme';
        }

        return 'unknown';
    }

    /**
     * Returns diagnostic provider metadata.
     * Strictly belongs to the 'WORDPRESS' backend profile (never creates a 3rd profile).
     *
     * @return array
     */
    public static function get_provider_metadata(): array {
        $mode = self::get_provider_mode();
        return [
            'mode'              => $mode,
            'backendProfile'    => 'WORDPRESS',
            'kernelVersion'     => defined( '\Carmilla_Kernel::VERSION' ) ? \Carmilla_Kernel::VERSION : '1.0.0',
            'themeVersion'      => defined( 'CARMILLA_THEME_VERSION' ) ? CARMILLA_THEME_VERSION : null,
            'pluginVersion'     => defined( 'CB_VERSION' ) ? CB_VERSION : ( defined( 'CB_PLUGIN_VERSION' ) ? CB_PLUGIN_VERSION : null ),
            'wooCommerceActive' => class_exists( 'WooCommerce' ),
            'activeTheme'       => function_exists( 'wp_get_theme' ) ? (string) wp_get_theme()->get( 'Name' ) : '',
        ];
    }

    /**
     * Generates canonical manifest payload matching KMP RemoteFeatureManifestPayload.
     *
     * @return array
     */
    public static function get_manifest_payload(): array {
        $provider_meta = self::get_provider_metadata();
        
        $features = [];
        if ( class_exists( '\Carmilla\Core\Catalog\CapabilityResolver' ) ) {
            $admin_input = function_exists( 'get_option' ) ? get_option( 'cb_manifest_features', [] ) : [];
            $features    = \Carmilla\Core\Catalog\CapabilityResolver::resolve_effective( [
                'admin_settings' => ! empty( $admin_input ) && is_array( $admin_input ) ? $admin_input : null,
            ] );
        } elseif ( class_exists( '\Carmilla_Kernel' ) ) {
            $features = \Carmilla_Kernel::get_active_features();
        }

        return [
            'schemaVersion'     => 1,
            'manifestVersion'   => function_exists( 'get_option' ) ? (string) get_option( 'cb_manifest_version', '2026.08.1' ) : '2026.08.1',
            'backendProfile'    => 'WORDPRESS',
            'hostMode'          => $provider_meta['mode'],
            'providerMetadata'  => $provider_meta,
            'tenantId'          => function_exists( 'get_current_blog_id' ) ? sanitize_key( (string) get_option( 'cb_manifest_tenant_id', get_current_blog_id() ) ) : '1',
            'minimumAppVersion' => function_exists( 'get_option' ) ? (string) get_option( 'cb_manifest_min_client', '1.0.0' ) : '1.0.0',
            'issuedAt'          => gmdate( 'c' ),
            'expiresAt'         => gmdate( 'c', time() + ( defined( 'DAY_IN_SECONDS' ) ? DAY_IN_SECONDS : 86400 ) ),
            'features'          => $features,
        ];
    }

    /**
     * REST callback for canonical client-manifest route.
     *
     * @param \WP_REST_Request|null $request
     * @return \WP_REST_Response
     */
    public static function get_manifest_response( $request = null ) {
        $payload = self::get_manifest_payload();
        $etag    = '"' . hash( 'sha256', function_exists( 'wp_json_encode' ) ? wp_json_encode( $payload ) : json_encode( $payload ) ) . '"';

        if ( is_object( $request ) && method_exists( $request, 'get_header' ) && $request->get_header( 'if-none-match' ) === $etag ) {
            $response = new \WP_REST_Response( null, 304 );
            $response->header( 'ETag', $etag );
            return $response;
        }

        $response = new \WP_REST_Response( $payload, 200 );
        $response->header( 'ETag', $etag );
        $response->header( 'Cache-Control', 'no-cache, max-age=0, must-revalidate' );
        return $response;
    }

    /**
     * Registers a REST route under the canonical Carmilla namespace.
     *
     * @param string $route    Route pattern (e.g. '/products', '/settings').
     * @param array  $args     Route options including methods, callback, permission_callback, and args.
     * @param bool   $override Whether to override existing route with the same pattern.
     * @return bool True on success, false on failure.
     */
    public static function register_route( string $route, array $args, bool $override = false ): bool {
        if ( ! function_exists( 'register_rest_route' ) ) {
            return false;
        }

        return register_rest_route( self::NAMESPACE, $route, $args, $override );
    }

    /**
     * Generates a standardized error response envelope matching KMP ApiError.
     *
     * Envelope format:
     * {
     *   "message": "...",
     *   "status": 400,
     *   "code": 400,
     *   "errorCode": "...",
     *   "path": "...",
     *   "timestamp": "2026-...",
     *   "success": false,
     *   "error": {
     *     "code": "...",
     *     "message": "..."
     *   }
     * }
     *
     * @param string $code            Machine-readable error slug (e.g. 'BAD_CREDENTIALS', 'VALIDATION').
     * @param string $message         Human-readable error description.
     * @param int    $status          HTTP status code (default: 400).
     * @param string $path            Endpoint path triggering the error.
     * @param array  $additional_data Additional context fields merged into the payload.
     * @return \WP_REST_Response|array
     */
    public static function error_response(
        string $code,
        string $message,
        int $status = 400,
        string $path = '',
        array $additional_data = []
    ) {
        $error_data = array_merge( [
            'code'    => $code,
            'message' => $message,
        ], $additional_data );

        $payload = array_merge( [
            'message'   => $message,
            'status'    => (string) $status,
            'code'      => $status,
            'errorCode' => $code,
            'path'      => $path,
            'timestamp' => gmdate( 'c' ),
            'success'   => false,
            'error'     => $error_data,
        ], $additional_data );

        if ( class_exists( '\WP_REST_Response' ) ) {
            return new \WP_REST_Response( $payload, $status );
        }

        return $payload;
    }

    /**
     * Generates a standardized success response envelope.
     *
     * @param mixed $data    Response payload data.
     * @param int   $status  HTTP status code (default: 200).
     * @param array $headers Optional HTTP headers map.
     * @return \WP_REST_Response|array
     */
    public static function success_response( $data, int $status = 200, array $headers = [] ) {
        $payload = [
            'success' => true,
            'data'    => $data,
        ];

        if ( class_exists( '\WP_REST_Response' ) ) {
            $response = new \WP_REST_Response( $payload, $status );
            foreach ( $headers as $key => $value ) {
                $response->header( (string) $key, (string) $value );
            }
            return $response;
        }

        return $payload;
    }

    /**
     * Generates a standardized paginated response envelope with pagination headers.
     * Fully compatible with KMP PageResponse<T>, BlogListResponse, and standard pagination.
     *
     * Sets standard headers:
     * - X-WP-Total: Total number of matching items
     * - X-WP-TotalPages: Total pages calculated from total and per_page
     *
     * Envelope format:
     * {
     *   "content": [...],
     *   "items": [...],
     *   "data": [...],
     *   "page": {
     *     "size": 20,
     *     "number": 1,
     *     "totalElements": 100,
     *     "totalPages": 5
     *   },
     *   "pagination": {
     *     "total": 100,
     *     "page": 1,
     *     "per_page": 20,
     *     "total_pages": 5
     *   },
     *   "totalPages": 5,
     *   "totalElements": 100,
     *   "success": true
     * }
     *
     * @param array $data     Items array for current page.
     * @param int   $total    Total items count across all pages.
     * @param int   $page     Current page number (1-based).
     * @param int   $per_page Items count per page.
     * @param int   $status   HTTP status code (default: 200).
     * @return \WP_REST_Response|array
     */
    public static function paginated_response(
        array $data,
        int $total,
        int $page,
        int $per_page,
        int $status = 200
    ) {
        $per_page    = self::sanitize_per_page( $per_page );
        $page        = max( 1, $page );
        $total       = max( 0, $total );
        $total_pages = $per_page > 0 ? (int) ceil( $total / $per_page ) : 1;
        if ( $total_pages < 1 ) {
            $total_pages = 1;
        }

        $payload = [
            'success'       => true,
            'content'       => $data,
            'items'         => $data,
            'data'          => $data,
            'page'          => [
                'size'          => $per_page,
                'number'        => $page,
                'totalElements' => $total,
                'totalPages'    => $total_pages,
            ],
            'pagination'    => [
                'total'       => $total,
                'page'        => $page,
                'per_page'    => $per_page,
                'total_pages' => $total_pages,
            ],
            'totalPages'    => $total_pages,
            'totalElements' => $total,
        ];

        if ( class_exists( '\WP_REST_Response' ) ) {
            $response = new \WP_REST_Response( $payload, $status );
            $response->header( 'X-WP-Total', (string) $total );
            $response->header( 'X-WP-TotalPages', (string) $total_pages );
            return $response;
        }

        return $payload;
    }

    /**
     * Validates the WordPress REST API nonce from an incoming request.
     *
     * @param mixed $request WP_REST_Request instance or array containing headers/params.
     * @return bool True if nonce is valid for 'wp_rest', false otherwise.
     */
    public static function validate_nonce( $request ): bool {
        $nonce = '';

        if ( is_object( $request ) ) {
            if ( method_exists( $request, 'get_header' ) ) {
                $nonce = $request->get_header( 'x_wp_nonce' ) ?: $request->get_header( 'X-WP-Nonce' );
            }
            if ( empty( $nonce ) && method_exists( $request, 'get_param' ) ) {
                $nonce = $request->get_param( '_wpnonce' );
            }
        } elseif ( is_array( $request ) ) {
            $nonce = $request['X-WP-Nonce'] ?? $request['x_wp_nonce'] ?? $request['_wpnonce'] ?? '';
        }

        if ( empty( $nonce ) || ! is_string( $nonce ) ) {
            return false;
        }

        if ( function_exists( 'wp_verify_nonce' ) ) {
            return (bool) wp_verify_nonce( $nonce, 'wp_rest' );
        }

        return false;
    }

    /**
     * Sanitizes and caps per_page query parameters against the ceiling.
     *
     * @param mixed $per_page Requested per_page value.
     * @param int   $default  Default fallback value (default: 10).
     * @param int   $max      Maximum ceiling allowed (default: MAX_PER_PAGE).
     * @return int Sanitized integer between 1 and $max.
     */
    public static function sanitize_per_page( $per_page, int $default = 10, int $max = self::MAX_PER_PAGE ): int {
        $val = is_numeric( $per_page ) ? (int) $per_page : $default;
        return max( 1, min( $val, $max ) );
    }

    /**
     * Maps public REST paths to canonical feature identifiers.
     *
     * @param string $route
     * @return string|null Canonical feature identifier or null if unrestricted.
     */
    public static function feature_for_route( $route ): ?string {
        $route = '/' . ltrim( (string) $route, '/' );
        $api_pos = strpos( $route, '/api/' );
        if ( false === $api_pos ) {
            return null;
        }
        $path = substr( $route, $api_pos );

        $map = [
            '/api/admin/psych-tests' => 'psych.tests',
            '/api/psych-tests'       => 'psych.tests',
            '/api/my-psych-tests'    => 'psych.tests',
            '/api/admin/therapists'  => 'clinic.booking',
            '/api/therapists'        => 'clinic.booking',
            '/api/clinic'            => 'clinic.booking',
            '/api/admin/courses'     => 'academy.core',
            '/api/courses'           => 'academy.core',
            '/api/academy'           => 'academy.core',
            '/api/my-courses'        => 'academy.core',
            '/api/course-requests'   => 'academy.core',
            '/api/admin/blogs'       => 'content.blog',
            '/api/blogs'             => 'content.blog',
            '/api/admin/pages'       => 'content.core',
            '/api/pages'             => 'content.core',
            '/api/admin/media'       => 'content.core',
            '/api/stories'           => 'content.stories',
            '/api/support'           => 'content.support',
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
            '/api/payment'           => 'commerce.core',
            '/api/reviews'           => 'commerce.core',
            '/api/questions'         => 'commerce.core',
            '/api/bundles'           => 'commerce.core',
            '/api/wallet'            => 'wallet',
            '/api/admin'             => 'admin.mobile',
        ];

        foreach ( $map as $prefix => $feature ) {
            if ( $path === $prefix || strpos( $path, $prefix . '/' ) === 0 ) {
                return $feature;
            }
        }

        return null;
    }

    /**
     * Enforce the manifest policy at the REST dispatch boundary.
     * Dispatches HTTP 403 FEATURE_DISABLED if the feature is inactive.
     */
    public static function guard_rest_request( $result, $server, $request ) {
        if ( $result !== null ) {
            return $result;
        }
        if ( ! is_object( $request ) || ! method_exists( $request, 'get_route' ) ) {
            return $result;
        }

        $feature = self::feature_for_route( $request->get_route() );
        if ( $feature === null ) {
            return $result;
        }

        $active = false;
        if ( class_exists( '\Carmilla_Kernel' ) && \Carmilla_Kernel::is_booted() ) {
            $active = \Carmilla_Kernel::is_feature_active( $feature );
        } elseif ( class_exists( '\Carmilla\Core\Catalog\CapabilityResolver' ) ) {
            $active = \Carmilla\Core\Catalog\CapabilityResolver::is_feature_active( $feature );
        }

        if ( $active ) {
            return $result;
        }

        if ( class_exists( '\WP_Error' ) ) {
            return new \WP_Error(
                'FEATURE_DISABLED',
                __( 'این قابلیت در حال حاضر فعال نیست.', 'carmilla' ),
                [ 'status' => 403, 'feature' => $feature ]
            );
        }

        return self::error_response( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر فعال نیست.', 403, (string) $request->get_route() );
    }

    /**
     * Intercepts legacy host-root aliases (/api/users/me, /api/addresses, etc.)
     * and maps them seamlessly to canonical carmilla/v1 routes with deprecation notice.
     */
    public static function handle_root_aliases(): void {
        $uri = isset( $_SERVER['REQUEST_URI'] ) ? (string) wp_parse_url( $_SERVER['REQUEST_URI'], PHP_URL_PATH ) : '';
        $uri = '/' . ltrim( untrailingslashit( $uri ), '/' );
        
        $root_paths = [ '/api/users/me', '/api/addresses', '/api/favorites', '/api/recently-viewed' ];
        $match      = null;
        foreach ( $root_paths as $rp ) {
            if ( $uri === $rp || strpos( $uri, $rp . '/' ) === 0 ) {
                $match = $rp;
                break;
            }
        }

        if ( null === $match ) {
            return;
        }

        // Set deprecation telemetry headers
        if ( ! headers_sent() ) {
            header( 'X-Carmilla-Deprecated: root-alias' );
            header( 'Warning: 299 - "Legacy root alias is deprecated and will be removed in the next client cycle"' );
        }

        if ( isset( $GLOBALS['wp'] ) && is_object( $GLOBALS['wp'] ) ) {
            $GLOBALS['wp']->query_vars['rest_route'] = '/' . self::NAMESPACE . $uri;
        }
        if ( function_exists( 'rest_api_loaded' ) ) {
            rest_api_loaded();
        }
    }

    /**
     * Injects diagnostic provider headers into outgoing REST responses.
     */
    public static function add_provider_headers( $result, $server, $request ) {
        if ( is_object( $result ) && method_exists( $result, 'header' ) ) {
            $mode = self::get_provider_mode();
            $result->header( 'X-Carmilla-Provider-Mode', $mode );
            $result->header( 'X-Carmilla-Backend-Profile', 'WORDPRESS' );
        }
        return $result;
    }

    /**
     * Authenticate requests carrying an Authorization: Bearer <jwt> header.
     * Hooks into determine_current_user.
     *
     * @param int|false $user_id Current user ID resolved so far.
     * @return int|false
     */
    public static function authenticate_bearer( $user_id ) {
        if ( ! empty( $user_id ) ) {
            return $user_id;
        }

        $token = self::get_bearer_token();
        if ( empty( $token ) ) {
            return $user_id;
        }

        // Try JwtService first
        $payload = JwtService::decode( $token );
        if ( ! $payload && class_exists( 'CB_JWT' ) ) {
            $payload = \CB_JWT::decode( $token );
        }

        $uid = ( $payload && ( $payload['typ'] ?? '' ) === 'access' ) ? (int) ( $payload['uid'] ?? 0 ) : 0;
        if ( $uid > 0 && function_exists( 'get_user_by' ) && get_user_by( 'id', $uid ) ) {
            return $uid;
        }

        return $user_id;
    }

    /**
     * Retrieves the Bearer token from request headers.
     *
     * @return string|null
     */
    public static function get_bearer_token(): ?string {
        $header = '';
        if ( isset( $_SERVER['HTTP_AUTHORIZATION'] ) ) {
            $header = $_SERVER['HTTP_AUTHORIZATION'];
        } elseif ( isset( $_SERVER['REDIRECT_HTTP_AUTHORIZATION'] ) ) {
            $header = $_SERVER['REDIRECT_HTTP_AUTHORIZATION'];
        } elseif ( function_exists( 'getallheaders' ) ) {
            $headers = getallheaders();
            $header  = $headers['Authorization'] ?? $headers['authorization'] ?? '';
        }

        if ( ! empty( $header ) && preg_match( '/Bearer\s+(.+)$/i', $header, $matches ) ) {
            return trim( $matches[1] );
        }

        return null;
    }

    /**
     * Reset state for isolation testing.
     */
    public static function reset_state(): void {
        self::$initialized = false;
    }
}
}
