<?php
/**
 * Test template pagination (wp_link_pages) and searchform integration.
 * Task: P02-THEME-CODE-010C3D2
 */

define( 'ABSPATH', __DIR__ . '/' );

$theme_dir = dirname( __DIR__ );
$failed = 0;
$passed = 0;

function assert_check( $condition, $label ) {
	global $failed, $passed;
	if ( $condition ) {
		echo "PASS: {$label}\n";
		$passed++;
	} else {
		echo "FAIL: {$label}\n";
		$failed++;
	}
}

// 1. Static code assertions
$templates_with_pagination = array(
	'single.php',
	'page.php',
	'single-cb_course.php',
	'single-cb_psychtest.php',
	'single-cb_therapist.php',
);

foreach ( $templates_with_pagination as $file ) {
	$content = file_get_contents( $theme_dir . '/' . $file );
	assert_check( false !== strpos( $content, 'wp_link_pages' ), "{$file} invokes wp_link_pages" );
}

$footer_content = file_get_contents( $theme_dir . '/footer.php' );
assert_check( false === strpos( $footer_content, '<form role="search"' ), 'footer.php does not hardcode role="search" form' );
assert_check( false !== strpos( $footer_content, 'get_search_form()' ), 'footer.php invokes get_search_form()' );

// 2. CSS rules check
$css_content = file_get_contents( $theme_dir . '/assets/css/base.css' );
assert_check( false !== strpos( $css_content, '.page-links' ), 'base.css provides .page-links styling' );

// 3. Runtime template execution check with synthetic stubs
$wp_link_pages_called = 0;
$wp_link_pages_args = array();
function wp_link_pages( $args = array() ) {
	global $wp_link_pages_called, $wp_link_pages_args;
	$wp_link_pages_called++;
	$wp_link_pages_args = $args;
	echo '<nav class="page-links-mocked"></nav>';
}

$get_search_form_called = 0;
function get_search_form( $echo = true ) {
	global $get_search_form_called;
	$get_search_form_called++;
	if ( $echo ) {
		echo '<form role="search" class="mocked-search"></form>';
	}
}

function esc_html__( $text, $domain = 'default' ) { return $text; }
function esc_attr__( $text, $domain = 'default' ) { return $text; }
function esc_html_e( $text, $domain = 'default' ) { echo $text; }
function esc_attr_e( $text, $domain = 'default' ) { echo $text; }
function esc_html( $text ) { return htmlspecialchars( (string) $text, ENT_QUOTES, 'UTF-8' ); }
function esc_attr( $text ) { return htmlspecialchars( (string) $text, ENT_QUOTES, 'UTF-8' ); }
function esc_url( $url ) { return filter_var( $url, FILTER_SANITIZE_URL ); }
function home_url( $path = '' ) { return 'https://example.test' . $path; }
function get_header() {}
function get_footer() {}
function post_class() { echo 'class="post"'; }
function the_title() { echo 'Sample Post Title'; }
function get_the_author() { return 'Test Author'; }
function get_the_date() { return '2026-11-11'; }
function carmilla_to_persian_digits( $str ) { return $str; }
function get_the_content() { return 'Post content for pagination test'; }
function the_content() { echo '<p>' . get_the_content() . '</p>'; }
function wp_strip_all_tags( $string ) { return strip_tags( (string) $string ); }
function has_post_thumbnail() { return false; }
function has_nav_menu( $location ) { return false; }
function wp_list_pages( $args ) {}
function date_i18n( $format ) { return date( $format ); }
function bloginfo( $show ) { echo 'Carmilla'; }
function get_bloginfo( $show = '' ) { return 'Carmilla Description'; }
function untrailingslashit( $string ) { return rtrim( (string) $string, '/\\' ); }
function add_query_arg( $args, $url = '' ) { return $url; }
function carmilla_bottom_nav_items() { return array(); }
function carmilla_icon( $name, $size = 20 ) { return ''; }
function wp_footer() {}
$nav_items = array();
$current = '';
function comments_open() { return false; }
function get_comments_number() { return 0; }
function get_the_category() { return array(); }

// Mock loop: exactly one post
$posts_loop = array( true, false );
function have_posts() {
	global $posts_loop;
	return array_shift( $posts_loop ) ?? false;
}
function the_post() {}

// Execute single.php buffer
ob_start();
include $theme_dir . '/single.php';
$output_single = ob_get_clean();

assert_check( $wp_link_pages_called === 1, 'single.php executed wp_link_pages in loop' );
assert_check( isset( $wp_link_pages_args['before'] ) && false !== strpos( $wp_link_pages_args['before'], 'page-links' ), 'wp_link_pages received proper before arg' );

// Execute page.php buffer
$posts_loop = array( true, false );
$wp_link_pages_called = 0;
ob_start();
include $theme_dir . '/page.php';
$output_page = ob_get_clean();

assert_check( $wp_link_pages_called === 1, 'page.php executed wp_link_pages in loop' );

// Execute footer.php buffer
ob_start();
include $theme_dir . '/footer.php';
$output_footer = ob_get_clean();

assert_check( $get_search_form_called === 1, 'footer.php executed get_search_form()' );
assert_check( false !== strpos( $output_footer, 'mocked-search' ), 'footer.php output contains rendered searchform' );

echo "\nSummary: {$passed} passed, {$failed} failed.\n";
exit( 0 === $failed ? 0 : 1 );
