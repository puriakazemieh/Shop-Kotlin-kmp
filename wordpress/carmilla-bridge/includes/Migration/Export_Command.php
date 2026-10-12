<?php
namespace Carmilla\Bridge\Migration;

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class Export_Command {

    public static function execute( $args, $assoc_args ): void {
        global $wpdb;

        $output_dir     = $assoc_args['output'] ?? wp_upload_dir()['basedir'] . '/carmilla-export';
        $customer_uuid  = $assoc_args['customer_uuid'] ?? '';
        $encryption_key = $assoc_args['encryption_key'] ?? '';
        $expiry         = isset( $assoc_args['expiry'] ) ? (int) $assoc_args['expiry'] : time() + DAY_IN_SECONDS;

        if ( '' === $customer_uuid || '' === $encryption_key ) {
            \WP_CLI::error( 'A customer UUID and a 32-byte encryption key are required for export.' );
            return;
        }
        if ( ! wp_mkdir_p( $output_dir ) ) {
            \WP_CLI::error( "Could not create export directory: {$output_dir}" );
            return;
        }

        $posts_file = trailingslashit( $output_dir ) . 'posts.ndjson';
        $media_file = trailingslashit( $output_dir ) . 'media.ndjson';
        $posts_handle = fopen( $posts_file, 'wb' );
        $media_handle = fopen( $media_file, 'wb' );
        if ( false === $posts_handle || false === $media_handle ) {
            if ( is_resource( $posts_handle ) ) {
                fclose( $posts_handle );
            }
            if ( is_resource( $media_handle ) ) {
                fclose( $media_handle );
            }
            \WP_CLI::error( 'Could not open migration export files.' );
            return;
        }

        $denied_types = implode( ',', array_map( static fn( $type ) => "'" . esc_sql( $type ) . "'", Migration_Config::get_denied_post_types() ) );
        $posts = $wpdb->get_results(
            "SELECT ID, post_title, post_content, post_type, post_status, post_parent FROM {$wpdb->posts} WHERE post_type NOT IN ({$denied_types})"
        );
        $posts_payload = '';
        foreach ( $posts as $post ) {
            $record = array(
                'id'        => (int) $post->ID,
                'title'     => $post->post_title,
                'content'   => $post->post_content,
                'type'      => $post->post_type,
                'status'    => $post->post_status,
                'parent_id' => (int) $post->post_parent,
                'meta'      => self::exportable_meta( (int) $post->ID, $wpdb ),
            );
            $line = wp_json_encode( $record, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES ) . "\n";
            fwrite( $posts_handle, $line );
            $posts_payload .= $line;
        }
        fclose( $posts_handle );

        $media_payload = '';
        $attachments = $wpdb->get_results( "SELECT ID, post_title, guid FROM {$wpdb->posts} WHERE post_type = 'attachment'" );
        foreach ( $attachments as $attachment ) {
            $attachment_path = get_attached_file( $attachment->ID );
            $record = array(
                'id'       => (int) $attachment->ID,
                'title'    => $attachment->post_title,
                'url'      => $attachment->guid,
                'checksum' => $attachment_path && file_exists( $attachment_path ) ? md5_file( $attachment_path ) : null,
            );
            $line = wp_json_encode( $record, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES ) . "\n";
            fwrite( $media_handle, $line );
            $media_payload .= $line;
        }
        fclose( $media_handle );

        file_put_contents( trailingslashit( $output_dir ) . 'posts.enc', Migration_Crypto::encrypt_payload( $posts_payload, $encryption_key, $customer_uuid, $expiry ), LOCK_EX );
        file_put_contents( trailingslashit( $output_dir ) . 'media.enc', Migration_Crypto::encrypt_payload( $media_payload, $encryption_key, $customer_uuid, $expiry ), LOCK_EX );
        unlink( $posts_file );
        unlink( $media_file );
        \WP_CLI::success( "Encrypted export completed: {$output_dir}" );
    }

    private static function exportable_meta( int $post_id, $wpdb ): array {
        $meta_rows = $wpdb->get_results( $wpdb->prepare( "SELECT meta_key, meta_value FROM {$wpdb->postmeta} WHERE post_id = %d", $post_id ) );
        $meta = array();
        foreach ( $meta_rows as $row ) {
            if ( Migration_Config::is_denied_meta_key( $row->meta_key ) ) {
                continue;
            }
            $meta[ $row->meta_key ] = $row->meta_value;
        }

        return $meta;
    }
}

if ( defined( 'WP_CLI' ) && WP_CLI ) {
    \WP_CLI::add_command( 'carmilla export-legacy', array( Export_Command::class, 'execute' ) );
}
