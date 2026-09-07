<?php
namespace Carmilla\Core\Import;

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

/**
 * Shared Importer for Seed Packs.
 * Lives in Carmilla Core, so both Theme and Plugin can use it.
 */
class Carmilla_Importer {

    private $manifest;
    private $state_key = 'carmilla_import_state';

    public function __construct($manifest_path = null) {
        if ($manifest_path && file_exists($manifest_path)) {
            $this->manifest = json_decode(file_get_contents($manifest_path), true);
        }
    }

    /**
     * Executes the import based on the provided JSON manifest.
     * @param bool $dry_run If true, only calculates counts without executing.
     * @return array|\WP_Error Results array with counts.
     */
    public function process_import(bool $dry_run = false) {
        if (empty($this->manifest) || !isset($this->manifest['chunks'])) {
            return new \WP_Error('invalid_manifest', 'Manifest is missing or invalid.');
        }

        $state = get_option($this->state_key, []);
        
        $results = [
            'create' => 0,
            'update' => 0,
            'skip' => 0,
            'conflict' => 0
        ];

        foreach ($this->manifest['chunks'] as $chunk) {
            $feature = $chunk['feature'];
            $checksum = $chunk['checksum'] ?? md5(json_encode($chunk));

            // Skip if feature is not active/licensed
            if (!$this->is_feature_active($feature)) {
                $results['skip']++;
                continue;
            }

            // Idempotency: Skip if already imported with the same checksum
            if (isset($state[$feature]) && $state[$feature] === $checksum) {
                $results['skip']++;
                continue;
            }

            // If state exists but checksum differs, it's an update/conflict (oversimplified)
            if (isset($state[$feature])) {
                $results['update']++;
            } else {
                $results['create']++;
            }

            if (!$dry_run) {
                // Process based on schema type
                $result = $this->import_chunk($chunk);

                if (!is_wp_error($result)) {
                    $state[$feature] = $checksum;
                    update_option($this->state_key, $state);
                } else {
                    $results['conflict']++;
                    if (isset($state[$feature])) {
                        $results['update']--;
                    } else {
                        $results['create']--;
                    }
                }
            }
        }

        return $results;
    }

    private function is_feature_active($feature) {
        // Abstract check for SKU/Feature
        $active_features = apply_filters('carmilla_active_features', ['core', 'messaging', 'payment']);
        return in_array($feature, $active_features, true);
    }

    private function import_chunk($chunk) {
        $schema = $chunk['schema'] ?? '';
        $data = $chunk['data'] ?? [];

        switch ($schema) {
            case 'wp_options':
                foreach ($data as $key => $value) {
                    if (get_option($key) === false) {
                        update_option($key, $value);
                    }
                }
                break;
            case 'wp_posts':
                foreach ($data as $post_data) {
                    // Check if post already exists via a custom meta to ensure idempotency
                    $existing = get_posts([
                        'meta_key' => '_carmilla_seed_id',
                        'meta_value' => $post_data['seed_id'],
                        'post_type' => 'any',
                        'post_status' => 'any',
                        'fields' => 'ids'
                    ]);
                    
                    if (empty($existing)) {
                        $post_id = wp_insert_post([
                            'post_title'   => $post_data['title'],
                            'post_content' => $post_data['content'],
                            'post_type'    => $post_data['type'] ?? 'post',
                            'post_status'  => 'publish'
                        ]);
                        if (!is_wp_error($post_id)) {
                            update_post_meta($post_id, '_carmilla_seed_id', $post_data['seed_id']);
                        }
                    }
                }
                break;
            default:
                // Hook for external chunk processing (e.g., WC products)
                do_action('carmilla_import_chunk_' . $schema, $data);
                break;
        }

        return true;
    }
}
