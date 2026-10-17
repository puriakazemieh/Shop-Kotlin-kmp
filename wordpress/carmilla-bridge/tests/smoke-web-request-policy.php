<?php
/** Pure decision tests. No WordPress site, database, route or HTTP server used. */
define( 'ABSPATH', __DIR__ . '/' );
require_once dirname( __DIR__ ) . '/includes/class-cb-web-request-policy.php';
$checks = 0; $failures = 0;
function check_policy( bool $condition, string $label ): void {
	global $checks, $failures;
	++$checks; if ( ! $condition ) { ++$failures; }
	echo ( $condition ? 'PASS ' : 'FAIL ' ) . $label . "\n";
}
function rejects_configuration( callable $factory ): bool {
	try { $factory(); return false; } catch ( InvalidArgumentException $e ) { return true; }
}
$policy = new CB_Web_Request_Policy( 'https://shop.example.test/wp', 1, array( 'https://shop.example.test', 'https://shop.example.test:8443' ), 'production' );
$headers = array( 'Host' => 'shop.example.test', 'Origin' => 'https://shop.example.test:8443', 'Content-Type' => 'application/json', 'X-Carmilla-Web-Request' => '1' );
$server = array( 'REMOTE_ADDR' => '192.0.2.42', 'SERVER_ADDR' => '192.0.2.1', 'HTTPS' => 'on' );
check_policy( $policy->allows_post( 'POST', $headers, $server ), 'configured same-host origin and direct HTTPS allowed' );
$actual_cors = $policy->cors_for_post( 'POST', $headers, $server );
check_policy( is_array( $actual_cors ) && $actual_cors['Access-Control-Allow-Origin'] === $headers['Origin'] && $actual_cors['Access-Control-Allow-Credentials'] === 'true' && $actual_cors['Vary'] === 'Origin', 'credentialed POST CORS decision reflects only the approved origin' );
$bad = $headers; $bad['Origin'] = 'https://evil.example.test';
check_policy( $policy->cors_for_post( 'POST', $bad, $server ) === null, 'hostile actual POST receives no CORS approval' );
$default_port = $headers; $default_port['Origin'] = 'HTTPS://SHOP.EXAMPLE.TEST:443';
check_policy( $policy->allows_post( 'POST', $default_port, $server ), 'scheme host and default port normalized' );
foreach ( array( null, '', 'null', 'https://evil.example.test', 'http://shop.example.test', 'https://shop.example.test:8444', 'https://shop.example.test/', 'https://user@shop.example.test', 'https://shop.example.test?x=1', 'https://shop.example.test#x', 'https://shop.example.test https://evil.example.test', 'https://shop.example.test,https://evil.example.test', 'https://shop.example.test:0', 'https://shop.example.test:65536', ' https://shop.example.test', array( 'https://shop.example.test', 'https://evil.example.test' ) ) as $value ) {
	$bad = $headers; $bad['Origin'] = $value;
	check_policy( ! $policy->allows_post( 'POST', $bad, $server ), 'missing malformed hostile or multiple origin rejected' );
}
$bad = $headers; unset( $bad['Origin'] );
check_policy( ! $policy->allows_post( 'POST', $bad, $server ), 'absent origin rejected' );
$bad = $headers; $bad['origin'] = $bad['Origin'];
check_policy( ! $policy->allows_post( 'POST', $bad, $server ), 'duplicate case-insensitive origin rejected' );
foreach ( array( 'GET', 'OPTIONS', 'post', '' ) as $method ) { check_policy( ! $policy->allows_post( $method, $headers, $server ), 'wrong request method rejected' ); }
foreach ( array( 'text/plain', 'application/x-www-form-urlencoded', 'multipart/form-data', 'application/json; charset=utf-8', '', array( 'application/json', 'application/json' ) ) as $content_type ) {
	$bad = $headers; $bad['Content-Type'] = $content_type;
	check_policy( ! $policy->allows_post( 'POST', $bad, $server ), 'non-exact or multiple content type rejected' );
}
$bad = $headers; unset( $bad['Content-Type'] );
check_policy( ! $policy->allows_post( 'POST', $bad, $server ), 'missing content type rejected' );
foreach ( array( '', '0', ' 1', '1, 1', array( '1', '1' ), "1\r\nX-Evil: yes" ) as $value ) {
	$bad = $headers; $bad['X-Carmilla-Web-Request'] = $value;
	check_policy( ! $policy->allows_post( 'POST', $bad, $server ), 'missing malformed or multiple custom header rejected' );
}
$bad = $headers; unset( $bad['X-Carmilla-Web-Request'] );
check_policy( ! $policy->allows_post( 'POST', $bad, $server ), 'absent custom header rejected' );
$bad = $headers; $bad['Host'] = 'evil.example.test';
check_policy( ! $policy->allows_post( 'POST', $bad, $server ), 'spoofed Host cannot change trusted identity' );
$bad = $headers; $bad['Host'] = array( 'shop.example.test', 'evil.example.test' );
check_policy( ! $policy->allows_post( 'POST', $bad, $server ), 'multiple host headers rejected' );
$bad_server = $server; unset( $bad_server['HTTPS'] );
check_policy( ! $policy->allows_post( 'POST', $headers, $bad_server ), 'production without HTTPS rejected' );
foreach ( array( 'Forwarded' => 'proto=https', 'X-Forwarded-Proto' => 'https', 'X-Forwarded-Host' => 'shop.example.test', 'X-Forwarded-For' => '127.0.0.1', 'X-Real-IP' => '127.0.0.1', 'X-Forwarded-Ssl' => 'on' ) as $name => $value ) {
	$bad = $headers; $bad[$name] = $value;
	check_policy( ! $policy->allows_post( 'POST', $bad, $server ), 'untrusted forwarding metadata rejected' );
}
check_policy( rejects_configuration( static function () { new CB_Web_Request_Policy( 'http://shop.example.test/wp', 1, array( 'http://shop.example.test' ), 'production', true ); } ), 'no production HTTP fallback' );
check_policy( rejects_configuration( static function () { new CB_Web_Request_Policy( 'http://localhost/wp', 1, array( 'http://localhost' ), 'local' ); } ), 'local HTTP exception defaults off' );
check_policy( rejects_configuration( static function () { new CB_Web_Request_Policy( 'http://localhost/wp', 1, array( 'http://localhost' ), 'production', true ); } ), 'local exception needs local environment' );
check_policy( rejects_configuration( static function () { new CB_Web_Request_Policy( 'http://127.0.0.1/wp', 1, array( 'http://127.0.0.1' ), 'local', true ); } ), 'local QA requires localhost host' );
check_policy( rejects_configuration( static function () { new CB_Web_Request_Policy( 'https://shop.example.test:/wp', 1, array( 'https://shop.example.test' ), 'production' ); } ), 'malformed configured site URL rejected' );
foreach ( array( array(), array( 'null' ), array( 'https://evil.example.test' ), array( 'https://sub.shop.example.test' ), array( 'http://shop.example.test' ), array( 'https://shop.example.test', 'HTTPS://SHOP.EXAMPLE.TEST:443' ), array( 'https://shop.example.test/path' ) ) as $origins ) {
	check_policy( rejects_configuration( static function () use ( $origins ) { new CB_Web_Request_Policy( 'https://shop.example.test/wp', 1, $origins, 'production' ); } ), 'invalid duplicate or untrusted origin configuration rejected' );
}
$local = new CB_Web_Request_Policy( 'http://localhost:8080/one', 1, array( 'http://localhost:8090' ), 'local', true );
$local_headers = array( 'Host' => 'localhost:8080', 'Origin' => 'http://localhost:8090', 'Content-Type' => 'application/json', 'X-Carmilla-Web-Request' => '1' );
$local_server = array( 'REMOTE_ADDR' => '127.0.0.1', 'SERVER_ADDR' => '::1', 'HTTPS' => '' );
check_policy( $local->allows_post( 'POST', $local_headers, $local_server ), 'explicit local QA and loopback allowed' );
foreach ( array( 'REMOTE_ADDR', 'SERVER_ADDR' ) as $field ) {
	$bad_server = $local_server; $bad_server[$field] = '192.0.2.1';
	check_policy( ! $local->allows_post( 'POST', $local_headers, $bad_server ), 'local QA rejects non-loopback address' );
	$bad_server = $local_server; unset( $bad_server[$field] );
	check_policy( ! $local->allows_post( 'POST', $local_headers, $bad_server ), 'local QA rejects unknown address' );
}
$bad = $local_headers; $bad['Host'] = 'localhost:8081';
check_policy( ! $local->allows_post( 'POST', $bad, $local_server ), 'local QA rejects wrong host or port' );
$bad = $local_headers; $bad['X-Forwarded-Proto'] = 'http';
check_policy( ! $local->allows_post( 'POST', $bad, $local_server ), 'local QA rejects proxy metadata' );
check_policy( rejects_configuration( static function () { new CB_Web_Request_Policy( 'https://shop.example.test', 1, array( 'https://shop.example.test' ), 'production', false, array( '192.0.2.5' ) ); } ), 'proxy requires explicit header-stripping declaration' );
$proxy = new CB_Web_Request_Policy( 'https://shop.example.test/wp', 1, array( 'https://shop.example.test:8443' ), 'production', false, array( '192.0.2.5' ), true );
$proxy_server = array( 'REMOTE_ADDR' => '192.0.2.5', 'SERVER_ADDR' => '192.0.2.1', 'HTTPS' => '' );
$proxy_headers = $headers; $proxy_headers['X-Forwarded-Proto'] = 'https';
check_policy( $proxy->allows_post( 'POST', $proxy_headers, $proxy_server ), 'declared stripping proxy with exact peer and https accepted' );
foreach ( array( 'http', 'https,http', array( 'https', 'https' ), 'HTTPS' ) as $value ) {
	$bad = $proxy_headers; $bad['X-Forwarded-Proto'] = $value;
	check_policy( ! $proxy->allows_post( 'POST', $bad, $proxy_server ), 'invalid forwarded protocol rejected' );
}
$bad_server = $proxy_server; $bad_server['REMOTE_ADDR'] = '192.0.2.6';
check_policy( ! $proxy->allows_post( 'POST', $proxy_headers, $bad_server ), 'unknown proxy rejected' );
$bad_server = $server;
check_policy( ! $proxy->allows_post( 'POST', $headers, $bad_server ), 'proxy mode rejects direct TLS fallback' );
check_policy( ! $proxy->allows_post( 'POST', $headers, $proxy_server ), 'proxy mode requires trusted forwarded protocol' );
$bad = $proxy_headers; $bad['X-Forwarded-Host'] = 'evil.example.test';
check_policy( ! $proxy->allows_post( 'POST', $bad, $proxy_server ), 'proxy host spoof rejected' );
$preflight = array( 'Host' => 'shop.example.test', 'Origin' => 'https://shop.example.test:8443', 'Access-Control-Request-Method' => 'POST', 'Access-Control-Request-Headers' => 'content-type, x-carmilla-web-request' );
$cors = $policy->preflight( 'OPTIONS', $preflight, $server );
check_policy( is_array( $cors ) && $cors['Access-Control-Allow-Origin'] === $preflight['Origin'] && $cors['Access-Control-Allow-Credentials'] === 'true' && strpos( $cors['Vary'], 'Origin' ) !== false, 'valid credentialed preflight returns exact origin and vary' );
foreach ( array( 'content-type', 'x-carmilla-web-request', 'content-type, x-carmilla-web-request, authorization', 'content-type, x-carmilla-web-request, content-type', '*', '' ) as $value ) {
	$bad = $preflight; $bad['Access-Control-Request-Headers'] = $value;
	check_policy( $policy->preflight( 'OPTIONS', $bad, $server ) === null, 'unapproved incomplete duplicate or wildcard preflight headers rejected' );
}
$bad = $preflight; $bad['Origin'] = 'https://evil.example.test';
check_policy( $policy->preflight( 'OPTIONS', $bad, $server ) === null, 'hostile CORS preflight rejected' );
$bad = $preflight; $bad['Access-Control-Request-Method'] = 'GET';
check_policy( $policy->preflight( 'OPTIONS', $bad, $server ) === null, 'wrong preflight method rejected' );
check_policy( $policy->preflight( 'POST', $preflight, $server ) === null, 'non-OPTIONS preflight rejected' );
$cookie = $policy->cookie( 2000, 1000 );
check_policy( $cookie['name'] === '__Host-carmilla_wp_' . substr( CB_Web_Session_WP_Adapter::site_id( 'https://shop.example.test/wp', 1 ), 0, 32 ) && $cookie['expires'] === 2000 && $cookie['path'] === '/' && $cookie['secure'] === true && $cookie['httponly'] === true && $cookie['samesite'] === 'Strict' && ! isset( $cookie['domain'] ), 'host cookie matches site identity and secure attributes' );
$other = new CB_Web_Request_Policy( 'https://shop.example.test/other', 1, array( 'https://shop.example.test' ), 'production' );
$different_blog = new CB_Web_Request_Policy( 'https://shop.example.test/wp', 2, array( 'https://shop.example.test' ), 'production' );
check_policy( $cookie['name'] !== $other->cookie( 2000, 1000 )['name'] && $cookie['name'] !== $different_blog->cookie( 2000, 1000 )['name'], 'installation and blog cookie names differ' );
check_policy( $local->cookie( 2000, 1000 )['secure'] === true, 'local QA does not weaken secure cookie attributes' );
check_policy( rejects_configuration( static function () use ( $policy ) { $policy->cookie( 1000, 1000 ); } ), 'expired cookie deadline rejected' );
check_policy( rejects_configuration( static function () use ( $policy ) { $policy->cookie( 1000, -1 ); } ), 'invalid clock rejected' );
echo "Checks: {$checks}; failures: {$failures}.\n";
exit( $failures ? 1 : 0 );
