<?php
/**
 * WordPress-backed smoke test for Phase 07 migration. Run with wp eval-file.
 */

use Carmilla\Bridge\Migration\Migrator;

if ( ! defined( 'ABSPATH' ) ) {
    require dirname( __DIR__, 4 ) . '/wp-load.php';
}

$failures = 0;
$source_site = 'p07-smoke-site';
$created_ids = array();

function p07_wordpress_check( bool $condition, string $message ): void {
    global $failures;
    echo ( $condition ? 'PASS' : 'FAIL' ) . "  {$message}\n";
    if ( ! $condition ) {
        ++$failures;
    }
}

try {
    $records = array(
        array(
            'id'      => 'parent-001',
            'title'   => 'P07 migration parent',
            'content' => 'https://legacy.example.test/parent',
            'type'    => 'post',
            'status'  => 'draft',
            'meta'    => array(
                'public_label'  => 'Synthetic parent',
                '_billing_email' => 'must-not-import@example.test',
            ),
        ),
        array(
            'id'        => 'child-001',
            'title'     => 'P07 migration child',
            'content'   => 'https://legacy.example.test/child',
            'type'      => 'post',
            'status'    => 'draft',
            'parent_id' => 'parent-001',
            'meta'      => array( 'public_label' => 'Synthetic child' ),
        ),
    );

    $migrator = new Migrator();
    $first = $migrator->process_pass_one( $records, $source_site, 'https://legacy.example.test', 'https://new.example.test' );
    p07_wordpress_check( 2 === $first['inserted'] && 0 === $first['rejected'], 'first pass inserts synthetic records' );
    $migrator->process_pass_two( $records, $source_site );

    $parent = $migrator->get_mapping( $source_site, 'post', 'parent-001' );
    $child = $migrator->get_mapping( $source_site, 'post', 'child-001' );
    $created_ids = array_filter( array( $parent->local_id ?? 0, $child->local_id ?? 0 ) );
    p07_wordpress_check( 2 === count( $created_ids ), 'source mappings are stored' );
    p07_wordpress_check( (int) get_post_field( 'post_parent', $child->local_id ) === (int) $parent->local_id, 'second pass restores parent relationship' );
    p07_wordpress_check( false !== strpos( get_post_field( 'post_content', $child->local_id ), 'https://new.example.test' ), 'legacy URLs are rewritten' );
    p07_wordpress_check( 'Synthetic parent' === get_post_meta( $parent->local_id, 'public_label', true ), 'eligible metadata is imported' );
    p07_wordpress_check( '' === get_post_meta( $parent->local_id, '_billing_email', true ), 'blocked metadata is not imported' );

    $second = $migrator->process_pass_one( $records, $source_site, 'https://legacy.example.test', 'https://new.example.test' );
    p07_wordpress_check( 2 === $second['skipped'], 'unchanged records are skipped on repeat import' );
} finally {
    global $wpdb;
    foreach ( $created_ids as $post_id ) {
        wp_delete_post( $post_id, true );
    }
    if ( isset( $wpdb ) ) {
        $wpdb->delete( $wpdb->prefix . 'carmilla_migration_map', array( 'source_site_uuid' => $source_site ), array( '%s' ) );
    }
}

exit( 0 === $failures ? 0 : 1 );
