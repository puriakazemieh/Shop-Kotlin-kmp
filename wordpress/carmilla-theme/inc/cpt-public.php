<?php
/**
 * Make the vertical CPTs (registered by the Carmilla Bridge plugin) render on the
 * front-end so the theme's single-/archive- templates work. story/banner/campaign
 * stay embedded-only (no standalone pages). Safe no-op if a CPT isn't registered.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

add_filter( 'register_post_type_args', function ( $args, $post_type ) {
	$front_end_cpts = array(
		'cb_course'         => 'courses',
		'cb_therapist'      => 'therapists',
		'cb_psychtest'      => 'psych-tests',
		'cb_course_request' => 'course-requests',
	);
	if ( isset( $front_end_cpts[ $post_type ] ) ) {
		$args['public']             = true;
		$args['publicly_queryable'] = true;
		$args['has_archive']        = true;
		$args['rewrite']            = array( 'slug' => $front_end_cpts[ $post_type ] );
	}
	return $args;
}, 20, 2 );
