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

    public static function execute( \, \ ) {
        global \;
        
        \ = \['output'] ?? wp_upload_dir()['basedir'] . '/carmilla-export';
        if (!file_exists(\)) {
            mkdir(\, 0755, true);
        }
        
        \ = \['customer_uuid'] ?? 'default-legacy-site';
        \ = \['encryption_key'] ?? '';
        \ = \['expiry'] ?? (time() + 86400); // Default 24h expiry

        \ = \ . '/posts.ndjson';
        \ = \ . '/media.ndjson';
        
        \ = fopen(\, 'w');
        \ = fopen(\, 'w');

        \WP_CLI::log('Exporting posts (excluding media)...');
        \ = \->get_results("SELECT ID, post_title, post_content, post_type, post_status FROM {\->posts} WHERE post_type NOT IN ('attachment', 'revision')");
        
        \ = '';
        foreach (\ as \) {
            \ = \->get_results(\->prepare("SELECT meta_key, meta_value FROM {\->postmeta} WHERE post_id = %d", \->ID));
            \ = [];
            foreach (\ as \) {
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
            
            \ = json_encode(\) . "\n";
            \ .= \;
            fwrite(\, \);
        }
        fclose(\);

        \WP_CLI::log('Exporting media manifest with checksums...');
        \ = \->get_results("SELECT ID, post_title, guid FROM {\->posts} WHERE post_type = 'attachment'");
        
        \ = '';
        foreach (\ as \) {
            \ = get_attached_file(\->ID);
            \ = file_exists(\) ? md5_file(\) : null;
            
            \ = [
                'id' => \->ID,
                'title' => \->post_title,
                'url' => \->guid,
                'checksum' => \
            ];
            \ = json_encode(\) . "\n";
            \ .= \;
            fwrite(\, \);
        }
        fclose(\);
        
        if (!empty(\)) {
            \WP_CLI::log('Encrypting payloads...');
            \ = Migration_Crypto::encrypt_payload(\, \, \, (int)\);
            \ = Migration_Crypto::encrypt_payload(\, \, \, (int)\);
            
            file_put_contents(\ . '/posts.enc', \);
            file_put_contents(\ . '/media.enc', \);
            
            // Delete raw ndjson to secure PII
            unlink(\);
            unlink(\);
            \WP_CLI::success("Export completed SECURELY (Encrypted) to: \");
        } else {
            \WP_CLI::success("Export completed securely (NDJSON) to: \. Warning: PII is unencrypted.");
        }
    }
}

if ( defined( 'WP_CLI' ) && WP_CLI ) {
    \WP_CLI::add_command( 'carmilla export-legacy', [ 'Carmilla\Bridge\Migration\Export_Command', 'execute' ] );
}
