<?php
/**
 * Admin meta boxes for the vertical CPTs — the in-dashboard management UI
 * (course/therapist/test fields). Native WordPress admin, no plugin needed.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/** Field config per post type: meta_key => [ label, type ]. */
function carmilla_meta_fields() {
	return array(
		'cb_course' => array(
			'title'  => 'مشخصات دوره',
			'fields' => array(
				'cb_instructor'   => array( 'مدرس', 'text' ),
				'cb_level'        => array( 'سطح', 'text' ),
				'cb_format'       => array( 'شکل برگزاری', 'select', array( 'ONLINE_RECORDED' => 'آنلاین (ضبط‌شده)', 'ONLINE_LIVE' => 'آنلاین (زنده)', 'IN_PERSON' => 'حضوری' ) ),
				'cb_duration'     => array( 'مدت', 'text' ),
				'cb_product_slug' => array( 'اسلاگ محصول WooCommerce (برای خرید)', 'text' ),
				'cb_syllabus'     => array( 'سرفصل‌ها (هر خط یک مورد)', 'textarea' ),
			),
		),
		'cb_therapist' => array(
			'title'  => 'مشخصات مشاور',
			'fields' => array(
				'cb_specialty'    => array( 'تخصص', 'text' ),
				'cb_approach'     => array( 'رویکرد درمانی', 'text' ),
				'cb_product_slug' => array( 'اسلاگ محصول (اعتبار جلسه؛ خالی = رایگان)', 'text' ),
				'cb_slots'        => array( 'بازه‌های زمانی (هر خط یک زمان، مثل 2026-08-01T14:00)', 'textarea' ),
			),
		),
		'cb_psychtest' => array(
			'title'  => 'مشخصات تست',
			'fields' => array(
				'cb_product_slug' => array( 'اسلاگ محصول (خرید تست؛ خالی = رایگان)', 'text' ),
				'cb_questions'    => array( 'سؤال‌ها — هر خط: «متن؟ | گزینه=امتیاز , گزینه=امتیاز»', 'textarea' ),
				'cb_ranges'       => array( 'بازه‌های نتیجه — هر خط: «حداقل | حداکثر | تفسیر»', 'textarea' ),
			),
		),
		'cb_banner' => array(
			'title'  => 'تنظیمات بنر',
			'fields' => array(
				'cb_subtitle'  => array( 'زیرعنوان', 'text' ),
				'cb_image_url' => array( 'آدرس تصویر (اختیاری)', 'url' ),
				'cb_link_url'  => array( 'لینک مقصد', 'url' ),
				'cb_sort'      => array( 'ترتیب', 'number' ),
			),
		),
		'cb_story' => array(
			'title'  => 'تنظیمات استوری',
			'fields' => array(
				'cb_link_url' => array( 'لینک مقصد', 'url' ),
			),
		),
	);
}

add_action( 'add_meta_boxes', function () {
	foreach ( carmilla_meta_fields() as $post_type => $conf ) {
		if ( post_type_exists( $post_type ) ) {
			add_meta_box( "carmilla_meta_$post_type", $conf['title'], 'carmilla_render_meta_box', $post_type, 'normal', 'high' );
		}
	}
} );

function carmilla_render_meta_box( $post ) {
	$conf = carmilla_meta_fields()[ $post->post_type ] ?? null;
	if ( ! $conf ) {
		return;
	}
	wp_nonce_field( 'carmilla_meta_save', 'carmilla_meta_nonce' );
	echo '<div style="display:grid;gap:12px;max-width:640px">';
	foreach ( $conf['fields'] as $key => $field ) {
		$label = $field[0];
		$type  = $field[1];
		$val   = get_post_meta( $post->ID, $key, true );
		echo '<p style="margin:0"><label style="display:block;font-weight:600;margin-bottom:4px">' . esc_html( $label ) . '</label>';
		if ( 'select' === $type ) {
			echo '<select name="' . esc_attr( $key ) . '" style="width:100%">';
			foreach ( ( $field[2] ?? array() ) as $opt_val => $opt_label ) {
				echo '<option value="' . esc_attr( $opt_val ) . '" ' . selected( $val, $opt_val, false ) . '>' . esc_html( $opt_label ) . '</option>';
			}
			echo '</select>';
		} elseif ( 'textarea' === $type ) {
			echo '<textarea name="' . esc_attr( $key ) . '" rows="5" style="width:100%">' . esc_textarea( $val ) . '</textarea>';
		} else {
			$input_type = in_array( $type, array( 'number', 'url' ), true ) ? $type : 'text';
			echo '<input type="' . esc_attr( $input_type ) . '" name="' . esc_attr( $key ) . '" value="' . esc_attr( $val ) . '" style="width:100%">';
		}
		echo '</p>';
	}
	echo '</div>';
}

add_action( 'save_post', function ( $post_id ) {
	if ( ! isset( $_POST['carmilla_meta_nonce'] ) || ! wp_verify_nonce( sanitize_key( $_POST['carmilla_meta_nonce'] ), 'carmilla_meta_save' ) ) {
		return;
	}
	if ( defined( 'DOING_AUTOSAVE' ) && DOING_AUTOSAVE ) {
		return;
	}
	if ( ! current_user_can( 'edit_post', $post_id ) ) {
		return;
	}
	$conf = carmilla_meta_fields()[ get_post_type( $post_id ) ] ?? null;
	if ( ! $conf ) {
		return;
	}
	foreach ( $conf['fields'] as $key => $field ) {
		if ( ! isset( $_POST[ $key ] ) ) {
			continue;
		}
		$raw  = wp_unslash( $_POST[ $key ] );
		$type = $field[1];
		if ( 'url' === $type ) {
			$clean = esc_url_raw( $raw );
		} elseif ( 'number' === $type ) {
			$clean = preg_replace( '/[^0-9.\-]/', '', $raw );
		} elseif ( 'textarea' === $type ) {
			$clean = sanitize_textarea_field( $raw );
		} else {
			$clean = sanitize_text_field( $raw );
		}
		update_post_meta( $post_id, $key, $clean );
	}
} );
