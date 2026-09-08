<?php
namespace Carmilla\Bridge\Migration;

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class Migrator {
    private \;
    
    public function __construct() {
        global \;
        \->db = \;
        \->create_table();
    }
    
    private function create_table() {
        \ = \->db->prefix . 'carmilla_migration_map';
        if (\->db->get_var("SHOW TABLES LIKE '\'") != \) {
            \ = \->db->get_charset_collate();
            \ = "CREATE TABLE \ (
                id bigint(20) NOT NULL AUTO_INCREMENT,
                source_site_uuid varchar(64) NOT NULL,
                source_object_id varchar(128) NOT NULL,
                object_type varchar(32) NOT NULL,
                local_id bigint(20) NOT NULL,
                record_hash varchar(32) DEFAULT NULL,
                PRIMARY KEY  (id),
                UNIQUE KEY source_mapping (source_site_uuid, source_object_id, object_type),
                KEY local_id (local_id)
            ) \;";
            require_once(ABSPATH . 'wp-admin/includes/upgrade.php');
            dbDelta(\);
        } else {
            // Ensure record_hash exists (schema update)
            \ = \->db->get_results("SHOW COLUMNS FROM '\' LIKE 'record_hash'");
            if (empty(\)) {
                \->db->query("ALTER TABLE \ ADD COLUMN record_hash varchar(32) DEFAULT NULL");
            }
        }
    }
    
    public function record_mapping(\, \, \, \, \ = null) {
        \ = \->db->prefix . 'carmilla_migration_map';
        \->db->replace(
            \,
            [
                'source_site_uuid' => \,
                'source_object_id' => \,
                'object_type'      => \,
                'local_id'         => \,
                'record_hash'      => \
            ]
        );
    }
    
    public function get_mapping(\, \, \) {
        \ = \->db->prefix . 'carmilla_migration_map';
        return \->db->get_row(\->db->prepare(
            "SELECT local_id, record_hash FROM \ WHERE source_site_uuid = %s AND object_type = %s AND source_object_id = %s",
            \, \, \
        ));
    }
    
    public function rewrite_urls(\, \, \) {
        if (empty(\) || empty(\) || empty(\)) {
            return \;
        }
        return str_replace(\, \, \);
    }
    
    public function process_pass_one(\, \, \ = '', \ = '') {
        \ = 0;
        \ = 0;
        \ = 0;

        foreach (\ as \) {
            \ = md5(json_encode(\));
            \ = \->get_mapping(\, 'post', \['id']);
            
            if (\ && \->record_hash === \) {
                // Delta import: skip if unchanged
                \++;
                continue;
            }
            
            \ = \->rewrite_urls(\['content'] ?? '', \, \);
            
            \ = [
                'post_title'   => \['title'] ?? '',
                'post_content' => \,
                'post_type'    => \['type'] ?? 'post',
                'post_status'  => \['status'] ?? 'publish',
            ];
            
            if (\ && \->local_id) {
                \['ID'] = \->local_id;
                wp_update_post(\);
                \->record_mapping(\, 'post', \['id'], \->local_id, \);
                \++;
            } else {
                \ = wp_insert_post(\);
                if (!is_wp_error(\)) {
                    \->record_mapping(\, 'post', \['id'], \, \);
                    \++;
                }
            }
        }
        
        return ['skipped' => \, 'updated' => \, 'inserted' => \];
    }
    
    public function process_pass_two(\, \) {
        foreach (\ as \) {
            \ = \->get_mapping(\, 'post', \['id']);
            if (!\ || !\->local_id) continue;
            
            if (!empty(\['parent_id'])) {
                \ = \->get_mapping(\, 'post', \['parent_id']);
                if (\ && \->local_id) {
                    wp_update_post([
                        'ID' => \->local_id,
                        'post_parent' => \->local_id
                    ]);
                }
            }
        }
    }
}
