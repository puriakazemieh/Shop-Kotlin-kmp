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
                PRIMARY KEY  (id),
                UNIQUE KEY source_mapping (source_site_uuid, source_object_id, object_type),
                KEY local_id (local_id)
            ) \;";
            require_once(ABSPATH . 'wp-admin/includes/upgrade.php');
            dbDelta(\);
        }
    }
    
    public function record_mapping(\, \, \, \) {
        \ = \->db->prefix . 'carmilla_migration_map';
        \->db->replace(
            \,
            [
                'source_site_uuid' => \,
                'source_object_id' => \,
                'object_type'      => \,
                'local_id'         => \
            ]
        );
    }
    
    public function get_local_id(\, \, \) {
        \ = \->db->prefix . 'carmilla_migration_map';
        return \->db->get_var(\->db->prepare(
            "SELECT local_id FROM \ WHERE source_site_uuid = %s AND object_type = %s AND source_object_id = %s",
            \, \, \
        ));
    }
    
    public function process_pass_one(\, \) {
        foreach (\ as \) {
            \ = \->get_local_id(\, 'post', \['id']);
            
            \ = [
                'post_title'   => \['title'],
                'post_content' => \['content'],
                'post_type'    => \['type'],
                'post_status'  => \['status'],
            ];
            
            if (\) {
                \['ID'] = \;
                wp_update_post(\);
                \ = \;
            } else {
                \ = wp_insert_post(\);
                if (!is_wp_error(\)) {
                    \->record_mapping(\, 'post', \['id'], \);
                }
            }
        }
    }
    
    public function process_pass_two(\, \) {
        foreach (\ as \) {
            \ = \->get_local_id(\, 'post', \['id']);
            if (!\) continue;
            
            if (!empty(\['parent_id'])) {
                \ = \->get_local_id(\, 'post', \['parent_id']);
                if (\) {
                    wp_update_post([
                        'ID' => \,
                        'post_parent' => \
                    ]);
                }
            }
        }
    }
}
