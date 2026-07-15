<?php
/**
 * Academy extras — the interactive learning screens the base theme didn't cover:
 *   - CourseQuizScreen        → final quiz (cb_quiz meta) with server scoring.
 *   - CertificatesScreen      → my-account «certificates» tab (issued on pass).
 *   - CertificateVerifyScreen → public [carmilla_verify] + REST lookup.
 *   - PlacementQuizScreen     → [carmilla_placement] leveling quiz.
 *   - ProjectSubmissionScreen → project submit (CPT cb_submission) on a course.
 *   - PeerReviewScreen        → peer comments on approved submissions.
 * Theme-only: course meta, user meta, a CPT, comments, and theme REST.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/* =========================================================================
 * Course quiz — cb_quiz lines: «سؤال؟ | گزینه درست* | گزینه | گزینه».
 * A trailing * (or ✓) marks the correct option. cb_pass_score = pass %.
 * ====================================================================== */

function carmilla_course_quiz( $course_id ) {
	$raw   = (string) get_post_meta( $course_id, 'cb_quiz', true );
	$lines = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ) );
	$out   = array();
	foreach ( $lines as $qi => $line ) {
		$parts = array_map( 'trim', explode( '|', $line ) );
		$text  = array_shift( $parts );
		if ( '' === $text || ! $parts ) {
			continue;
		}
		$options = array();
		$correct = 0;
		foreach ( array_values( $parts ) as $oi => $opt ) {
			if ( preg_match( '/[\*✓]\s*$/u', $opt ) ) {
				$correct = $oi;
				$opt     = trim( preg_replace( '/[\*✓]\s*$/u', '', $opt ) );
			}
			$options[] = $opt;
		}
		$out[] = array( 'index' => $qi, 'text' => $text, 'options' => $options, 'correct' => $correct );
	}
	return $out;
}

function carmilla_course_pass_score( $course_id ) {
	$s = (int) get_post_meta( $course_id, 'cb_pass_score', true );
	return $s > 0 ? min( 100, $s ) : 60;
}

add_action( 'rest_api_init', function () {
	register_rest_route( 'carmilla/v1', '/courses/(?P<id>\d+)/quiz', array(
		'methods' => 'GET', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_quiz_get',
	) );
	register_rest_route( 'carmilla/v1', '/courses/(?P<id>\d+)/quiz/submit', array(
		'methods' => 'POST', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_quiz_submit',
	) );
	register_rest_route( 'carmilla/v1', '/certificates/verify', array(
		'methods' => 'GET', 'permission_callback' => '__return_true', 'callback' => 'carmilla_rest_cert_verify',
	) );
	register_rest_route( 'carmilla/v1', '/placement/submit', array(
		'methods' => 'POST', 'permission_callback' => '__return_true', 'callback' => 'carmilla_rest_placement_submit',
	) );
	register_rest_route( 'carmilla/v1', '/courses/(?P<id>\d+)/project', array(
		array( 'methods' => 'GET', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_project_get' ),
		array( 'methods' => 'POST', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_project_submit' ),
	) );
	register_rest_route( 'carmilla/v1', '/courses/(?P<id>\d+)/peer', array(
		'methods' => 'GET', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_peer_list',
	) );
	register_rest_route( 'carmilla/v1', '/submissions/(?P<sid>\d+)/comments', array(
		array( 'methods' => 'GET', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_peer_comments_get' ),
		array( 'methods' => 'POST', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_peer_comments_post' ),
	) );
} );

/** Public questions (correct index stripped). */
function carmilla_rest_quiz_get( WP_REST_Request $req ) {
	$id = (int) $req['id'];
	if ( get_post_type( $id ) !== 'cb_course' ) {
		return new WP_Error( 'not_found', 'دوره یافت نشد.', array( 'status' => 404 ) );
	}
	$questions = array_map( function ( $q ) {
		return array( 'index' => $q['index'], 'text' => $q['text'], 'options' => $q['options'] );
	}, carmilla_course_quiz( $id ) );
	return rest_ensure_response( array(
		'title'     => get_the_title( $id ),
		'hasQuiz'   => ! empty( $questions ),
		'passScore' => carmilla_course_pass_score( $id ),
		'questions' => $questions,
	) );
}

function carmilla_rest_quiz_submit( WP_REST_Request $req ) {
	$id = (int) $req['id'];
	if ( get_post_type( $id ) !== 'cb_course' ) {
		return new WP_Error( 'not_found', 'دوره یافت نشد.', array( 'status' => 404 ) );
	}
	if ( ! carmilla_course_accessible( $id ) ) {
		return new WP_Error( 'forbidden', 'ابتدا در دوره ثبت‌نام کنید.', array( 'status' => 403 ) );
	}
	$answers   = (array) $req->get_param( 'answers' ); // index => chosen option
	$questions = carmilla_course_quiz( $id );
	if ( ! $questions ) {
		return new WP_Error( 'no_quiz', 'برای این دوره آزمونی تعریف نشده.', array( 'status' => 400 ) );
	}
	$correct = 0;
	foreach ( $questions as $q ) {
		$given = isset( $answers[ $q['index'] ] ) ? (int) $answers[ $q['index'] ] : -1;
		if ( $given === (int) $q['correct'] ) {
			$correct++;
		}
	}
	$score     = (int) round( $correct / count( $questions ) * 100 );
	$passScore = carmilla_course_pass_score( $id );
	$passed    = $score >= $passScore;

	$cert = null;
	if ( $passed ) {
		$cert = carmilla_issue_certificate( $id, get_current_user_id() );
	}
	return rest_ensure_response( array(
		'score'     => $score,
		'passScore' => $passScore,
		'passed'    => $passed,
		'certNumber' => $cert ? $cert['certNumber'] : null,
	) );
}

/* =========================================================================
 * Certificates — issued on quiz pass, listed in my-account, publicly verifiable.
 * ====================================================================== */

function carmilla_cert_number( $course_id, $user_id ) {
	$salt = wp_salt( 'auth' );
	return 'CB-' . strtoupper( substr( md5( $course_id . '-' . $user_id . '-' . $salt ), 0, 10 ) );
}

function carmilla_issue_certificate( $course_id, $user_id ) {
	$number = carmilla_cert_number( $course_id, $user_id );
	$certs  = get_user_meta( $user_id, 'cb_certs', true );
	$certs  = is_array( $certs ) ? $certs : array();
	foreach ( $certs as $c ) {
		if ( $c['certNumber'] === $number ) {
			return $c; // already issued
		}
	}
	$cert = array(
		'certNumber'  => $number,
		'courseId'    => $course_id,
		'courseTitle' => get_the_title( $course_id ),
		'issuedAt'    => current_time( 'c' ),
	);
	$certs[] = $cert;
	update_user_meta( $user_id, 'cb_certs', $certs );

	// Global index for public verification (no user data exposed).
	$index = get_option( 'cb_cert_index', array() );
	$index = is_array( $index ) ? $index : array();
	$index[ $number ] = array( 'courseTitle' => $cert['courseTitle'], 'issuedAt' => $cert['issuedAt'] );
	update_option( 'cb_cert_index', $index, false );

	return $cert;
}

function carmilla_rest_cert_verify( WP_REST_Request $req ) {
	$number = strtoupper( trim( sanitize_text_field( (string) $req->get_param( 'code' ) ) ) );
	$index  = get_option( 'cb_cert_index', array() );
	if ( is_array( $index ) && isset( $index[ $number ] ) ) {
		return rest_ensure_response( array(
			'valid'       => true,
			'courseTitle' => $index[ $number ]['courseTitle'],
			'issuedAt'    => $index[ $number ]['issuedAt'],
		) );
	}
	return rest_ensure_response( array( 'valid' => false ) );
}

add_action( 'init', function () {
	add_rewrite_endpoint( 'certificates', EP_ROOT | EP_PAGES );
	add_rewrite_endpoint( 'my-courses', EP_ROOT | EP_PAGES );
} );

add_filter( 'woocommerce_account_menu_items', function ( $items ) {
	if ( ! carmilla_feature_enabled( 'courses' ) ) {
		return $items;
	}
	$logout = isset( $items['customer-logout'] ) ? array( 'customer-logout' => $items['customer-logout'] ) : array();
	unset( $items['customer-logout'] );
	$items['my-courses']   = __( 'دوره‌های من', 'carmilla' );
	$items['certificates'] = __( 'گواهی‌های من', 'carmilla' );
	return array_merge( $items, $logout );
} );

/** My-account «my-courses» tab (← MyCoursesScreen): accessible courses + progress. */
add_action( 'woocommerce_account_my-courses_endpoint', function () {
	$ids = get_posts( array(
		'post_type'      => 'cb_course',
		'post_status'    => 'publish',
		'posts_per_page' => -1,
		'fields'         => 'ids',
	) );
	$mine = array();
	foreach ( $ids as $id ) {
		$slug = get_post_meta( $id, 'cb_product_slug', true );
		// "Enrolled" = bought the linked paid course, or has recorded progress on any course.
		$bought = $slug && function_exists( 'wc_customer_bought_product' ) && carmilla_course_accessible( $id );
		if ( $bought || carmilla_course_progress( $id ) ) {
			$mine[] = $id;
		}
	}
	if ( ! $mine ) {
		echo '<p class="t-body t-muted">' . esc_html__( 'هنوز در دوره‌ای ثبت‌نام نکرده‌اید.', 'carmilla' ) . ' <a href="' . esc_url( get_post_type_archive_link( 'cb_course' ) ) . '">' . esc_html__( 'مشاهده‌ی دوره‌ها', 'carmilla' ) . '</a></p>';
		return;
	}
	echo '<div class="cb-mycourses">';
	foreach ( $mine as $id ) {
		$pct = carmilla_course_percent( $id );
		echo '<a class="card card--pad cb-mycourse" href="' . esc_url( get_permalink( $id ) ) . '">';
		echo '<div class="cb-mycourse__head"><span class="t-title-sm">' . esc_html( get_the_title( $id ) ) . '</span>';
		echo '<span class="badge badge--rating">' . esc_html( carmilla_to_persian_digits( $pct ) ) . '٪</span></div>';
		echo '<div class="cb-mycourse__bar"><span class="cb-mycourse__fill" style="width:' . esc_attr( $pct ) . '%"></span></div>';
		echo '</a>';
	}
	echo '</div>';
} );

add_action( 'woocommerce_account_certificates_endpoint', function () {
	$certs = get_user_meta( get_current_user_id(), 'cb_certs', true );
	$certs = is_array( $certs ) ? $certs : array();
	if ( ! $certs ) {
		echo '<p class="t-body t-muted">' . esc_html__( 'هنوز گواهی‌ای ندارید. با قبولی در آزمونِ پایانِ دوره، گواهی صادر می‌شود.', 'carmilla' ) . '</p>';
		return;
	}
	foreach ( array_reverse( $certs ) as $c ) {
		echo '<div class="card card--pad cb-cert">';
		echo '<div class="cb-cert__head">' . carmilla_icon( 'academy', 26 ) . '<span class="t-title-sm">' . esc_html__( 'گواهیِ پایانِ دوره', 'carmilla' ) . '</span></div>';
		echo '<h3 class="t-title-sm cb-cert__course">' . esc_html( $c['courseTitle'] ) . '</h3>';
		echo '<p class="t-body-sm t-muted">' . esc_html__( 'شماره‌ی گواهی:', 'carmilla' ) . ' <strong>' . esc_html( $c['certNumber'] ) . '</strong></p>';
		echo '<p class="t-body-sm t-muted">' . esc_html__( 'تاریخِ صدور:', 'carmilla' ) . ' ' . esc_html( carmilla_to_persian_digits( mysql2date( 'Y-m-d', $c['issuedAt'] ) ) ) . '</p>';
		echo '</div>';
	}
} );

/** Public certificate verification page. */
add_shortcode( 'carmilla_verify', function () {
	ob_start();
	echo '<div id="cb-verify" class="cb-verify container container--readable">';
	echo '<p class="t-body-sm t-muted">' . esc_html__( 'شماره‌ی گواهی را وارد کنید تا اصالت و مشخصاتِ آن بررسی شود.', 'carmilla' ) . '</p>';
	echo '<div class="cb-verify__row"><input type="text" class="cb-verify__input" placeholder="' . esc_attr__( 'شماره‌ی گواهی', 'carmilla' ) . '">';
	echo '<button type="button" class="btn btn--primary cb-verify__btn">' . esc_html__( 'بررسیِ گواهی', 'carmilla' ) . '</button></div>';
	echo '<div class="cb-verify__result"></div>';
	echo '</div>';
	return ob_get_clean();
} );

/* =========================================================================
 * Placement quiz (← PlacementQuizScreen). Options carry a level weight (0/1/2).
 * Average weight → suggested level, with a link to courses at that level.
 * ====================================================================== */

function carmilla_placement_questions() {
	$default = array(
		array(
			'text'    => __( 'چقدر با موضوعِ این حوزه آشنایی دارید؟', 'carmilla' ),
			'options' => array(
				array( 'label' => __( 'تازه شروع کرده‌ام', 'carmilla' ), 'weight' => 0 ),
				array( 'label' => __( 'کمی تجربه دارم', 'carmilla' ), 'weight' => 1 ),
				array( 'label' => __( 'مسلط هستم', 'carmilla' ), 'weight' => 2 ),
			),
		),
		array(
			'text'    => __( 'آیا پروژه‌ی عملی انجام داده‌اید؟', 'carmilla' ),
			'options' => array(
				array( 'label' => __( 'خیر', 'carmilla' ), 'weight' => 0 ),
				array( 'label' => __( 'یکی دو مورد', 'carmilla' ), 'weight' => 1 ),
				array( 'label' => __( 'چندین پروژه', 'carmilla' ), 'weight' => 2 ),
			),
		),
		array(
			'text'    => __( 'هدفِ شما از این دوره چیست؟', 'carmilla' ),
			'options' => array(
				array( 'label' => __( 'یادگیریِ پایه', 'carmilla' ), 'weight' => 0 ),
				array( 'label' => __( 'تقویتِ مهارت', 'carmilla' ), 'weight' => 1 ),
				array( 'label' => __( 'تخصصی‌شدن', 'carmilla' ), 'weight' => 2 ),
			),
		),
	);
	return apply_filters( 'carmilla_placement_questions', $default );
}

function carmilla_placement_levels() {
	return array(
		'beginner'     => __( 'مبتدی', 'carmilla' ),
		'intermediate' => __( 'متوسط', 'carmilla' ),
		'advanced'     => __( 'پیشرفته', 'carmilla' ),
	);
}

function carmilla_rest_placement_submit( WP_REST_Request $req ) {
	$answers   = array_map( 'intval', (array) $req->get_param( 'answers' ) );
	$questions = carmilla_placement_questions();
	if ( ! $questions ) {
		return new WP_Error( 'no_quiz', 'سؤالی تعریف نشده.', array( 'status' => 400 ) );
	}
	$sum = 0;
	foreach ( $questions as $qi => $q ) {
		$oi     = isset( $answers[ $qi ] ) ? $answers[ $qi ] : 0;
		$weight = isset( $q['options'][ $oi ]['weight'] ) ? (int) $q['options'][ $oi ]['weight'] : 0;
		$sum   += $weight;
	}
	$avg   = $sum / count( $questions );
	$level = $avg < 0.67 ? 'beginner' : ( $avg < 1.34 ? 'intermediate' : 'advanced' );
	$labels = carmilla_placement_levels();
	return rest_ensure_response( array(
		'level' => $level,
		'label' => $labels[ $level ],
		'url'   => add_query_arg( 'level', $level, get_post_type_archive_link( 'cb_course' ) ?: home_url( '/' ) ),
	) );
}

add_shortcode( 'carmilla_placement', function () {
	$questions = array_map( function ( $q ) {
		return array( 'text' => $q['text'], 'options' => wp_list_pluck( $q['options'], 'label' ) );
	}, carmilla_placement_questions() );
	ob_start();
	echo '<div id="cb-placement" class="cb-placement container container--readable" data-questions="' . esc_attr( wp_json_encode( $questions ) ) . '"></div>';
	return ob_get_clean();
} );

/* =========================================================================
 * Project submission (CPT cb_submission) + peer review (comments on it).
 * ====================================================================== */

add_action( 'init', function () {
	register_post_type( 'cb_submission', array(
		'label'           => __( 'پروژه‌های دوره', 'carmilla' ),
		'public'          => false,
		'show_ui'         => true,
		'show_in_menu'    => 'edit.php?post_type=cb_course',
		'supports'        => array( 'title', 'editor' ),
		'capability_type' => 'post',
		'map_meta_cap'    => true,
	) );
} );

function carmilla_submission_dto( $post, $with_user = false ) {
	$out = array(
		'id'             => $post->ID,
		'courseId'       => (int) get_post_meta( $post->ID, 'cb_course_id', true ),
		'fileUrl'        => (string) get_post_meta( $post->ID, 'cb_file_url', true ),
		'note'           => $post->post_content,
		'status'         => get_post_meta( $post->ID, 'cb_status', true ) ?: 'PENDING',
		'mentorFeedback' => (string) get_post_meta( $post->ID, 'cb_mentor_feedback', true ),
	);
	if ( $with_user ) {
		$u = get_userdata( $post->post_author );
		$out['userName'] = $u ? $u->display_name : ( 'کاربر #' . $post->post_author );
	}
	return $out;
}

/** The current user's submission for a course (latest), or null. */
function carmilla_user_submission( $course_id, $user_id = 0 ) {
	$user_id = $user_id ?: get_current_user_id();
	$posts   = get_posts( array(
		'post_type'      => 'cb_submission',
		'post_status'    => 'publish',
		'posts_per_page' => 1,
		'author'         => $user_id,
		'meta_key'       => 'cb_course_id',
		'meta_value'     => $course_id,
	) );
	return $posts ? $posts[0] : null;
}

function carmilla_rest_project_get( WP_REST_Request $req ) {
	$course_id = (int) $req['id'];
	$post      = carmilla_user_submission( $course_id );
	return rest_ensure_response( array( 'submission' => $post ? carmilla_submission_dto( $post ) : null ) );
}

function carmilla_rest_project_submit( WP_REST_Request $req ) {
	$course_id = (int) $req['id'];
	if ( get_post_type( $course_id ) !== 'cb_course' ) {
		return new WP_Error( 'not_found', 'دوره یافت نشد.', array( 'status' => 404 ) );
	}
	$file_url = esc_url_raw( (string) $req->get_param( 'fileUrl' ) );
	if ( ! $file_url ) {
		return new WP_Error( 'validation', 'لینکِ پروژه را وارد کنید.', array( 'status' => 400 ) );
	}
	$note    = trim( wp_strip_all_tags( (string) $req->get_param( 'note' ) ) );
	$existing = carmilla_user_submission( $course_id );
	if ( $existing ) {
		wp_update_post( array( 'ID' => $existing->ID, 'post_content' => $note ) );
		update_post_meta( $existing->ID, 'cb_file_url', $file_url );
		update_post_meta( $existing->ID, 'cb_status', 'PENDING' );
		delete_post_meta( $existing->ID, 'cb_mentor_feedback' );
		$post = get_post( $existing->ID );
	} else {
		$id = wp_insert_post( array(
			'post_type'    => 'cb_submission',
			'post_status'  => 'publish',
			'post_author'  => get_current_user_id(),
			'post_title'   => get_the_title( $course_id ) . ' — ' . wp_get_current_user()->display_name,
			'post_content' => $note,
		) );
		if ( is_wp_error( $id ) || ! $id ) {
			return new WP_Error( 'create_failed', 'ثبت نشد.', array( 'status' => 400 ) );
		}
		update_post_meta( $id, 'cb_course_id', $course_id );
		update_post_meta( $id, 'cb_file_url', $file_url );
		update_post_meta( $id, 'cb_status', 'PENDING' );
		$post = get_post( $id );
	}
	return rest_ensure_response( array( 'submission' => carmilla_submission_dto( $post ) ) );
}

/** Approved peers' submissions for a course (excluding the current user). */
function carmilla_rest_peer_list( WP_REST_Request $req ) {
	$course_id = (int) $req['id'];
	$posts     = get_posts( array(
		'post_type'      => 'cb_submission',
		'post_status'    => 'publish',
		'posts_per_page' => 50,
		'author__not_in' => array( get_current_user_id() ),
		'meta_query'     => array(
			array( 'key' => 'cb_course_id', 'value' => $course_id ),
			array( 'key' => 'cb_status', 'value' => 'APPROVED' ),
		),
	) );
	return rest_ensure_response( array_map( function ( $p ) {
		return carmilla_submission_dto( $p, true );
	}, $posts ) );
}

function carmilla_rest_peer_comments_get( WP_REST_Request $req ) {
	$sid      = (int) $req['sid'];
	$comments = get_comments( array( 'post_id' => $sid, 'type' => 'cb_peer', 'status' => 'approve', 'order' => 'ASC' ) );
	return rest_ensure_response( array_map( function ( $c ) {
		return array( 'userName' => $c->comment_author, 'comment' => $c->comment_content );
	}, $comments ) );
}

function carmilla_rest_peer_comments_post( WP_REST_Request $req ) {
	$sid  = (int) $req['sid'];
	if ( get_post_type( $sid ) !== 'cb_submission' ) {
		return new WP_Error( 'not_found', 'یافت نشد.', array( 'status' => 404 ) );
	}
	$text = trim( wp_strip_all_tags( (string) $req->get_param( 'comment' ) ) );
	if ( '' === $text ) {
		return new WP_Error( 'validation', 'نظر خالی است.', array( 'status' => 400 ) );
	}
	$user = wp_get_current_user();
	$cid  = wp_insert_comment( array(
		'comment_post_ID'      => $sid,
		'comment_content'      => $text,
		'comment_type'         => 'cb_peer',
		'user_id'              => $user->ID,
		'comment_author'       => $user->display_name,
		'comment_author_email' => $user->user_email,
		'comment_approved'     => 1,
	) );
	if ( ! $cid ) {
		return new WP_Error( 'create_failed', 'ارسال نشد.', array( 'status' => 400 ) );
	}
	return rest_ensure_response( array( 'userName' => $user->display_name, 'comment' => $text ) );
}

/** Keep peer-review comments out of default comment queries/counts. */
add_filter( 'comments_clauses', function ( $clauses, $query ) {
	if ( ! is_admin() && empty( $query->query_vars['type'] ) ) {
		$clauses['where'] .= " AND comment_type != 'cb_peer'";
	}
	return $clauses;
}, 12, 2 );
