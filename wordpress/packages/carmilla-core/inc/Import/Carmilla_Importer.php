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
        $pack_id = $chunk['pack_id'] ?? 'default_pack';
        $feature = $chunk['feature'] ?? 'core';

        global $wpdb;

        switch ($schema) {
            case 'wp_options':
                foreach ($data as $key => $value) {
                    if (get_option($key) === false) {
                        update_option($key, $value);
                        $this->log_seed_object($pack_id, $feature, 'option', 0, $key);
                    }
                }
                break;
            case 'wp_posts':
                foreach ($data as $post_data) {
                    $seed_id = $post_data['seed_id'];
                    // Check registry
                    $existing_registry = $wpdb->get_var($wpdb->prepare(
                        "SELECT wp_entity_id FROM {$wpdb->prefix}carmilla_seed_objects WHERE seed_entity_id = %s AND wp_entity_type = 'post'",
                        $seed_id
                    ));

                    if (empty($existing_registry)) {
                        $post_id = wp_insert_post([
                            'post_title'   => $post_data['title'],
                            'post_content' => $post_data['content'],
                            'post_type'    => $post_data['type'] ?? 'post',
                            'post_status'  => 'publish'
                        ]);
                        if (!is_wp_error($post_id)) {
                            update_post_meta($post_id, '_carmilla_seed_id', $seed_id);
                            $this->log_seed_object($pack_id, $feature, 'post', $post_id, $seed_id);
                        }
                    } else {
                        // Skip update to preserve customer modifications, or do smart merge if necessary.
                        // For now, stable key exists, we skip to preserve.
                    }
                }
                break;
            default:
                do_action('carmilla_import_chunk_' . $schema, $data, $pack_id, $feature);
                break;
        }

        return true;
    }

    private function log_seed_object($pack_id, $feature, $wp_entity_type, $wp_entity_id, $seed_entity_id) {
        global $wpdb;
        $wpdb->insert(
            "{$wpdb->prefix}carmilla_seed_objects",
            [
                'pack_id' => $pack_id,
                'feature' => $feature,
                'wp_entity_type' => $wp_entity_type,
                'wp_entity_id' => $wp_entity_id,
                'seed_entity_id' => $seed_entity_id
            ],
            ['%s', '%s', '%s', '%d', '%s']
        );
    }
}
