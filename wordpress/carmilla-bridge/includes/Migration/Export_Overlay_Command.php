<?php
namespace Carmilla\Bridge\Migration;

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class Export_Overlay_Command {

    public static function execute( \, \ ) {
        global \;
        
        \ = \['output'] ?? wp_upload_dir()['basedir'] . '/carmilla-overlay';
        if (!file_exists(\)) {
            mkdir(\, 0755, true);
        }

        \ = \ . '/customer-overlay.ndjson';
        \ = fopen(\, 'w');

        \WP_CLI::log('Exporting customer overlay (modifications and new data)...');
        
        \ = "'" . implode("','", Migration_Config::get_denied_post_types()) . "'";
        
        \ = "
            SELECT p.ID, p.post_title, p.post_content, p.post_type, p.post_status, p.post_modified, s.created_at as seed_created
            FROM {\->posts} p
            LEFT JOIN {\->prefix}carmilla_seed_objects s ON p.ID = s.wp_entity_id AND s.wp_entity_type = 'post'
            WHERE p.post_type NOT IN (\)
            AND (s.id IS NULL OR p.post_modified > s.created_at)
        ";

        \ = \->get_results(\);
        \ = 0;
        \ = Migration_Config::get_denied_meta_keys();

        foreach (\ as \) {
            \ = \->get_results(\->prepare("SELECT meta_key, meta_value FROM {\->postmeta} WHERE post_id = %d", \->ID));
            \ = [];
            foreach (\ as \) {
                if (in_array(\->meta_key, \) || stripos(\->meta_key, 'token') !== false || stripos(\->meta_key, 'password') !== false) {
                    continue;
                }
                \[\->meta_key] = \->meta_value;
            }
            
            \ = [
                'id' => \->ID,
                'title' => \->post_title,
                'content' => \->post_content,
                'type' => \->post_type,
                'status' => \->post_status,
                'is_overlay' => true,
                'meta' => \
            ];
            
            fwrite(\, json_encode(\) . "\n");
            \++;
        }

        fclose(\);
        \WP_CLI::success("Exported \ overlay records to: \");
    }
}

if ( defined( 'WP_CLI' ) && WP_CLI ) {
    \WP_CLI::add_command( 'carmilla export-overlay', [ 'Carmilla\Bridge\Migration\Export_Overlay_Command', 'execute' ] );
}
