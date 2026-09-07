<?php
namespace Carmilla\Core\Rest;

/**
 * Unified REST Infrastructure
 *
 * Exposes standardized REST registration, structured envelopes for responses and errors,
 * pagination headers, and nonce validation across Carmilla Theme, Bridge Plugin, and KMP clients.
 *
 * Part of Task P04-WPPLUGIN-CODE-009.
 */
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
     * Registers a REST route under the canonical Carmilla namespace.
     *
     * @param string $route    Route pattern (e.g. '/products', '/settings').
     * @param array  $args     Route options including methods, callback, permission_callback, and args.
     * @param bool   $override Whether to override existing route with the same pattern.
     * @return bool True on success, false on failure.
     */
    public static function register_route(string $route, array $args, bool $override = false): bool {
        if (!function_exists('register_rest_route')) {
            return false;
        }

        return register_rest_route(self::NAMESPACE, $route, $args, $override);
    }

    /**
     * Generates a standardized error response envelope.
     *
     * Envelope format:
     * {
     *   "success": false,
     *   "error": {
     *     "code": "...",
     *     "message": "..."
     *   }
     * }
     *
     * @param string $code            Machine-readable error slug (e.g. 'not_found', 'forbidden').
     * @param string $message         Human-readable error description.
     * @param int    $status          HTTP status code (default: 400).
     * @param array  $additional_data Additional context fields merged into the error object.
     * @return \WP_REST_Response
     */
    public static function error_response(
        string $code,
        string $message,
        int $status = 400,
        array $additional_data = []
    ) {
        $error_data = array_merge([
            'code'    => $code,
            'message' => $message,
        ], $additional_data);

        $payload = [
            'success' => false,
            'error'   => $error_data,
        ];

        if (class_exists('\WP_REST_Response')) {
            return new \WP_REST_Response($payload, $status);
        }

        return $payload;
    }

    /**
     * Generates a standardized success response envelope.
     *
     * Envelope format:
     * {
     *   "success": true,
     *   "data": ...
     * }
     *
     * @param mixed $data    Response payload data.
     * @param int   $status  HTTP status code (default: 200).
     * @param array $headers Optional HTTP headers map.
     * @return \WP_REST_Response
     */
    public static function success_response($data, int $status = 200, array $headers = []) {
        $payload = [
            'success' => true,
            'data'    => $data,
        ];

        if (class_exists('\WP_REST_Response')) {
            $response = new \WP_REST_Response($payload, $status);
            foreach ($headers as $key => $value) {
                $response->header((string) $key, (string) $value);
            }
            return $response;
        }

        return $payload;
    }

    /**
     * Generates a standardized paginated response envelope with pagination headers.
     *
     * Sets standard headers:
     * - X-WP-Total: Total number of matching items
     * - X-WP-TotalPages: Total pages calculated from total and per_page
     *
     * Envelope format:
     * {
     *   "success": true,
     *   "data": [...],
     *   "pagination": {
     *     "total": 100,
     *     "page": 1,
     *     "per_page": 20,
     *     "total_pages": 5
     *   }
     * }
     *
     * @param array $data     Items array for current page.
     * @param int   $total    Total items count across all pages.
     * @param int   $page     Current page number (1-based).
     * @param int   $per_page Items count per page.
     * @param int   $status   HTTP status code (default: 200).
     * @return \WP_REST_Response
     */
    public static function paginated_response(
        array $data,
        int $total,
        int $page,
        int $per_page,
        int $status = 200
    ) {
        $per_page    = max(1, min($per_page, self::MAX_PER_PAGE));
        $page        = max(1, $page);
        $total       = max(0, $total);
        $total_pages = $per_page > 0 ? (int) ceil($total / $per_page) : 1;
        if ($total_pages < 1) {
            $total_pages = 1;
        }

        $payload = [
            'success'    => true,
            'data'       => $data,
            'pagination' => [
                'total'       => $total,
                'page'        => $page,
                'per_page'    => $per_page,
                'total_pages' => $total_pages,
            ],
        ];

        if (class_exists('\WP_REST_Response')) {
            $response = new \WP_REST_Response($payload, $status);
            $response->header('X-WP-Total', (string) $total);
            $response->header('X-WP-TotalPages', (string) $total_pages);
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
    public static function validate_nonce($request): bool {
        $nonce = '';

        if (is_object($request)) {
            if (method_exists($request, 'get_header')) {
                $nonce = $request->get_header('x_wp_nonce') ?: $request->get_header('X-WP-Nonce');
            }
            if (empty($nonce) && method_exists($request, 'get_param')) {
                $nonce = $request->get_param('_wpnonce');
            }
        } elseif (is_array($request)) {
            $nonce = $request['X-WP-Nonce'] ?? $request['x_wp_nonce'] ?? $request['_wpnonce'] ?? '';
        }

        if (empty($nonce) || !is_string($nonce)) {
            return false;
        }

        if (function_exists('wp_verify_nonce')) {
            return (bool) wp_verify_nonce($nonce, 'wp_rest');
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
    public static function sanitize_per_page($per_page, int $default = 10, int $max = self::MAX_PER_PAGE): int {
        $val = is_numeric($per_page) ? (int) $per_page : $default;
        return max(1, min($val, $max));
    }
}
