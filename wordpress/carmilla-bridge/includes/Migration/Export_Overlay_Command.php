<?php
namespace Carmilla\Bridge\Migration;

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class Export_Overlay_Command {

    public static function execute( $args, $assoc_args ): void {
        global $wpdb;

        $output_dir     = $assoc_args['output'] ?? wp_upload_dir()['basedir'] . '/carmilla-overlay';
        $customer_uuid  = $assoc_args['customer_uuid'] ?? '';
        $encryption_key = $assoc_args['encryption_key'] ?? '';
        $expiry         = isset( $assoc_args['expiry'] ) ? (int) $assoc_args['expiry'] : time() + DAY_IN_SECONDS;
        if ( '' === $customer_uuid || '' === $encryption_key ) {
            \WP_CLI::error( 'A customer UUID and a 32-byte encryption key are required for overlay export.' );
            return;
        }
        if ( ! wp_mkdir_p( $output_dir ) ) {
            \WP_CLI::error( "Could not create overlay directory: {$output_dir}" );
            return;
        }

        $denied_types = implode( ',', array_map( static fn( $type ) => "'" . esc_sql( $type ) . "'", Migration_Config::get_denied_post_types() ) );
        $seed_table = $wpdb->prefix . 'carmilla_seed_objects';
        $sql = "
            SELECT p.ID, p.post_title, p.post_content, p.post_type, p.post_status, p.post_parent
            FROM {$wpdb->posts} p
            LEFT JOIN {$seed_table} s ON p.ID = s.wp_entity_id AND s.wp_entity_type = 'post'
            WHERE p.post_type NOT IN ({$denied_types})
              AND (s.id IS NULL OR p.post_modified_gmt > s.created_at)
        ";
        $records = $wpdb->get_results( $sql );
        if ( null === $records ) {
            \WP_CLI::error( 'Could not select records for overlay export: ' . $wpdb->last_error );
            return;
        }

        $payload = '';
        foreach ( $records as $post ) {
            $record = array(
                'id'         => (int) $post->ID,
                'title'      => $post->post_title,
                'content'    => $post->post_content,
                'type'       => $post->post_type,
                'status'     => $post->post_status,
                'parent_id'  => (int) $post->post_parent,
                'is_overlay' => true,
                'meta'       => self::exportable_meta( (int) $post->ID, $wpdb ),
            );
            $payload .= wp_json_encode( $record, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES ) . "\n";
        }

        $output_file = trailingslashit( $output_dir ) . 'customer-overlay.enc';
        file_put_contents( $output_file, Migration_Crypto::encrypt_payload( $payload, $encryption_key, $customer_uuid, $expiry ), LOCK_EX );
        \WP_CLI::success( 'Encrypted ' . count( $records ) . " overlay records to: {$output_file}" );
    }

    private static function exportable_meta( int $post_id, $wpdb ): array {
        $rows = $wpdb->get_results( $wpdb->prepare( "SELECT meta_key, meta_value FROM {$wpdb->postmeta} WHERE post_id = %d", $post_id ) );
        $meta = array();
        foreach ( $rows as $row ) {
            if ( ! Migration_Config::is_denied_meta_key( $row->meta_key ) ) {
                $meta[ $row->meta_key ] = $row->meta_value;
            }
        }

        return $meta;
    }
}

if ( defined( 'WP_CLI' ) && WP_CLI ) {
    \WP_CLI::add_command( 'carmilla export-overlay', array( Export_Overlay_Command::class, 'execute' ) );
}
