<?php
namespace Carmilla\Bridge\Migration;

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class Migrator {
    private $db;

    public function __construct( $database = null ) {
        global $wpdb;
        $this->db = $database ?: $wpdb;
        if ( ! $this->db ) {
            throw new \RuntimeException( 'WordPress database access is required for migration.' );
        }
        $this->create_table();
    }

    private function create_table(): void {
        $table = $this->db->prefix . 'carmilla_migration_map';
        $exists = $this->db->get_var( $this->db->prepare( 'SHOW TABLES LIKE %s', $table ) );
        if ( $table === $exists ) {
            return;
        }

        require_once ABSPATH . 'wp-admin/includes/upgrade.php';
        $charset_collate = $this->db->get_charset_collate();
        dbDelta(
            "CREATE TABLE {$table} (
                id bigint(20) unsigned NOT NULL AUTO_INCREMENT,
                source_site_uuid varchar(64) NOT NULL,
                source_object_id varchar(128) NOT NULL,
                object_type varchar(32) NOT NULL,
                local_id bigint(20) unsigned NOT NULL,
                record_hash char(32) DEFAULT NULL,
                PRIMARY KEY (id),
                UNIQUE KEY source_mapping (source_site_uuid, source_object_id, object_type),
                KEY local_id (local_id)
            ) {$charset_collate};"
        );
    }

    public function record_mapping( string $source_site_uuid, string $object_type, string $source_object_id, int $local_id, string $record_hash = null ): void {
        $this->db->replace(
            $this->db->prefix . 'carmilla_migration_map',
            array(
                'source_site_uuid' => $source_site_uuid,
                'source_object_id' => $source_object_id,
                'object_type'      => $object_type,
                'local_id'         => $local_id,
                'record_hash'      => $record_hash,
            ),
            array( '%s', '%s', '%s', '%d', '%s' )
        );
    }

    public function get_mapping( string $source_site_uuid, string $object_type, string $source_object_id ) {
        $table = $this->db->prefix . 'carmilla_migration_map';
        return $this->db->get_row(
            $this->db->prepare(
                "SELECT local_id, record_hash FROM {$table} WHERE source_site_uuid = %s AND object_type = %s AND source_object_id = %s",
                $source_site_uuid,
                $object_type,
                $source_object_id
            )
        );
    }

    public function rewrite_urls( string $content, string $legacy_domain, string $new_domain ): string {
        if ( '' === $content || '' === $legacy_domain || '' === $new_domain ) {
            return $content;
        }

        return str_replace( $legacy_domain, $new_domain, $content );
    }

    public function process_pass_one( array $records, string $source_site_uuid, string $legacy_domain = '', string $new_domain = '' ): array {
        $results = array( 'skipped' => 0, 'updated' => 0, 'inserted' => 0, 'rejected' => 0 );

        foreach ( $records as $record ) {
            if ( ! is_array( $record ) || ! isset( $record['id'], $record['type'] ) || Migration_Config::is_denied_post_type( $record['type'] ) ) {
                ++$results['rejected'];
                continue;
            }

            $source_id = (string) $record['id'];
            $record_hash = md5( wp_json_encode( $record ) );
            $mapping = $this->get_mapping( $source_site_uuid, 'post', $source_id );
            if ( $mapping && hash_equals( (string) $mapping->record_hash, $record_hash ) ) {
                ++$results['skipped'];
                continue;
            }

            $post_data = array(
                'post_title'   => $record['title'] ?? '',
                'post_content' => $this->rewrite_urls( (string) ( $record['content'] ?? '' ), $legacy_domain, $new_domain ),
                'post_type'    => $record['type'],
                'post_status'  => $record['status'] ?? 'draft',
            );
            if ( $mapping && ! empty( $mapping->local_id ) ) {
                $post_data['ID'] = (int) $mapping->local_id;
                $local_id = wp_update_post( $post_data, true );
                $operation = 'updated';
            } else {
                $local_id = wp_insert_post( $post_data, true );
                $operation = 'inserted';
            }
            if ( is_wp_error( $local_id ) || ! $local_id ) {
                ++$results['rejected'];
                continue;
            }

            $this->sync_meta( (int) $local_id, $record['meta'] ?? array() );
            $this->record_mapping( $source_site_uuid, 'post', $source_id, (int) $local_id, $record_hash );
            ++$results[ $operation ];
        }

        return $results;
    }

    public function process_pass_two( array $records, string $source_site_uuid ): void {
        foreach ( $records as $record ) {
            if ( ! is_array( $record ) || empty( $record['parent_id'] ) || ! isset( $record['id'] ) ) {
                continue;
            }
            $child = $this->get_mapping( $source_site_uuid, 'post', (string) $record['id'] );
            $parent = $this->get_mapping( $source_site_uuid, 'post', (string) $record['parent_id'] );
            if ( $child && $parent && ! empty( $child->local_id ) && ! empty( $parent->local_id ) ) {
                wp_update_post( array( 'ID' => (int) $child->local_id, 'post_parent' => (int) $parent->local_id ), true );
            }
        }
    }

    private function sync_meta( int $post_id, $meta ): void {
        if ( ! is_array( $meta ) ) {
            return;
        }
        foreach ( $meta as $key => $value ) {
            if ( ! is_string( $key ) || Migration_Config::is_denied_meta_key( $key ) ) {
                continue;
            }
            update_post_meta( $post_id, $key, $value );
        }
    }
}
