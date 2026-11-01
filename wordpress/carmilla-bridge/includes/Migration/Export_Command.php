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
        foreach ( array( 'posts.ndjson', 'media.ndjson' ) as $legacy_name ) {
            if ( file_exists( trailingslashit( $output_dir ) . $legacy_name ) ) {
                \WP_CLI::error( 'A legacy plaintext export exists in the output directory. Securely remove it before retrying.' );
                return;
            }
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
            $posts_payload .= $line;
        }

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
            $media_payload .= $line;
        }

        $posts_temp = null;
        $media_temp = null;
        $export_error = null;
        try {
            $posts_encrypted = Migration_Crypto::encrypt_payload( $posts_payload, $encryption_key, $customer_uuid, $expiry );
            $media_encrypted = Migration_Crypto::encrypt_payload( $media_payload, $encryption_key, $customer_uuid, $expiry );
            $posts_temp = trailingslashit( $output_dir ) . 'posts.enc.' . bin2hex( random_bytes( 8 ) ) . '.tmp';
            $media_temp = trailingslashit( $output_dir ) . 'media.enc.' . bin2hex( random_bytes( 8 ) ) . '.tmp';
            if ( strlen( $posts_encrypted ) !== @file_put_contents( $posts_temp, $posts_encrypted, LOCK_EX )
                || strlen( $media_encrypted ) !== @file_put_contents( $media_temp, $media_encrypted, LOCK_EX )
                || ! @rename( $posts_temp, trailingslashit( $output_dir ) . 'posts.enc' )
                || ! @rename( $media_temp, trailingslashit( $output_dir ) . 'media.enc' ) ) {
                throw new \RuntimeException( 'Could not write encrypted migration export files.' );
            }
        } catch ( \Throwable $exception ) {
            $export_error = $exception->getMessage();
        } finally {
            if ( null !== $posts_temp && file_exists( $posts_temp ) ) {
                if ( ! @unlink( $posts_temp ) ) {
                    $export_error = 'Could not remove a temporary encrypted export file.';
                }
            }
            if ( null !== $media_temp && file_exists( $media_temp ) ) {
                if ( ! @unlink( $media_temp ) ) {
                    $export_error = 'Could not remove a temporary encrypted export file.';
                }
            }
        }
        if ( null !== $export_error ) {
            \WP_CLI::error( 'Encrypted migration export failed: ' . $export_error );
            return;
        }
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
