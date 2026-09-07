<?php
// smoke-messaging.php - Standalone tests for P06-MESSAGE capabilities
echo "Running Messaging Smoke Tests...\n";

// 1. Mock dependencies
$GLOBALS['wp_options'] = [
    'carmilla_message_settings' => [
        'sms_api_url' => 'https://api.sms.com/send',
        'sms_api_key' => 'fake_key', // Not encrypted for simplicity of test
        'sms_method' => 'POST'
    ]
];

function get_option($key, $default = false) {
    return $GLOBALS['wp_options'][$key] ?? $default;
}

function esc_html($str) { return htmlspecialchars($str); }
function sanitize_text_field($str) { return trim(strip_tags($str)); }
function wp_kses_post($str) { return strip_tags($str, '<b><i><strong><em><a>'); }

$GLOBALS['remote_requests'] = [];
function wp_safe_remote_request($url, $args) {
    // SSRF check
    if (strpos($url, '127.0.0.1') !== false || strpos($url, 'localhost') !== false) {
        return ['error' => 'SSRF rejected'];
    }
    if (strpos($url, 'https://') !== 0) {
        return ['error' => 'HTTPS required'];
    }
    
    $GLOBALS['remote_requests'][] = [
        'url' => $url,
        'args' => $args
    ];
    return ['response' => ['code' => 200], 'body' => '{"message_id": "test_123"}'];
}

function is_wp_error($obj) { return isset($obj['error']); }
function wp_remote_retrieve_response_code($response) { return $response['response']['code']; }
function wp_remote_retrieve_body($response) { return $response['body']; }

// Load classes
require_once __DIR__ . '/../../packages/carmilla-core/inc/Settings/MessageSettings.php';
require_once __DIR__ . '/../includes/class-cb-sms-http-adapter.php';
require_once __DIR__ . '/../includes/class-cb-template-engine.php';
require_once __DIR__ . '/../includes/class-cb-message-queue.php';

// Test 1: SSRF Defense
echo "Test 1: SSRF Defense (Localhost)...\n";
$GLOBALS['wp_options']['carmilla_message_settings']['sms_api_url'] = 'http://127.0.0.1/admin';
$result = CB_SMS_HTTP_Adapter::send('09123456789', 'test');
if (is_wp_error($result) && strpos($result['error'], 'HTTPS') !== false) {
    echo "PASS: Non-HTTPS rejected.\n";
} else {
    echo "FAIL: SSRF Check failed.\n";
    exit(1);
}

// Test 2: Valid HTTPS Request & Masking
echo "Test 2: Valid HTTPS Request and Masking...\n";
$GLOBALS['wp_options']['carmilla_message_settings']['sms_api_url'] = 'https://api.sms.com/send';
$result = CB_SMS_HTTP_Adapter::send('09123456789', 'test');
if ($result['success'] === true && $result['masked_key_used'] === 'fake***') {
    echo "PASS: Sent successfully with masked key.\n";
} else {
    echo "FAIL: Valid request failed.\n";
    exit(1);
}

// Test 3: Template Engine RTL & Sanitization
echo "Test 3: Template Engine RTL & Sanitization...\n";
$html = CB_Template_Engine::render('Hello {{name}}', ['name' => '<script>alert(1)</script>'], [], true);
if (strpos($html, '<script>') === false && strpos($html, 'dir="rtl"') !== false) {
    echo "PASS: Template sanitized and RTL applied.\n";
} else {
    echo "FAIL: Template sanitization failed.\n";
    exit(1);
}

echo "All Messaging Smoke Tests PASSED.\n";
exit(0);
