<?php
namespace Carmilla\Bridge\Migration;

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

/**
 * WP-CLI Command to export legacy data securely to NDJSON.
 * Prevents the use of insecure PHP serialized objects.
 */
class Export_Command {

    /**
     * Executes the export.
     * 
     * ## OPTIONS
     * 
     * [--output=<path>]
     * : Path to the output directory. Default is wp-content/uploads/carmilla-export.
     * 
     * @param array \
     * @param array \
     */
    public static function execute( \, \ ) {
        global \;
        
        \ = \['output'] ?? wp_upload_dir()['basedir'] . '/carmilla-export';
        if (!file_exists(\)) {
            mkdir(\, 0755, true);
        }

        \ = \ . '/posts.ndjson';
        \ = \ . '/media.ndjson';
        
        \ = fopen(\, 'w');
        \ = fopen(\, 'w');

        \WP_CLI::log('Exporting posts (excluding media)...');
        \ = \->get_results("SELECT ID, post_title, post_content, post_type, post_status FROM {\->posts} WHERE post_type NOT IN ('attachment', 'revision')");
        foreach (\ as \) {
            // Fetch meta securely, decode serialized if needed safely, or just export raw strings
            \ = \->get_results(\->prepare("SELECT meta_key, meta_value FROM {\->postmeta} WHERE post_id = %d", \->ID));
            \ = [];
            foreach (\ as \) {
                // We do NOT unserialize here to prevent object injection on the other side.
                // The migrator on Carmilla side will handle mapping.
                \[\->meta_key] = \->meta_value;
            }
            
            \ = [
                'id' => \->ID,
                'title' => \->post_title,
                'content' => \->post_content,
                'type' => \->post_type,
                'status' => \->post_status,
                'meta' => \
            ];
            
            fwrite(\, json_encode(\) . "\n");
        }

        \WP_CLI::log('Exporting media manifest with checksums...');
        \ = \->get_results("SELECT ID, post_title, guid FROM {\->posts} WHERE post_type = 'attachment'");
        foreach (\ as \) {
            \ = get_attached_file(\->ID);
            \ = file_exists(\) ? md5_file(\) : null;
            
            \ = [
                'id' => \->ID,
                'title' => \->post_title,
                'url' => \->guid,
                'checksum' => \
            ];
            fwrite(\, json_encode(\) . "\n");
        }

        fclose(\);
        fclose(\);

        \WP_CLI::success("Export completed securely (NDJSON) to: \");
    }
}

if ( defined( 'WP_CLI' ) && WP_CLI ) {
    \WP_CLI::add_command( 'carmilla export-legacy', [ 'Carmilla\Bridge\Migration\Export_Command', 'execute' ] );
}
