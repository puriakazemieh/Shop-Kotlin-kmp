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

    private $cursor_key = 'carmilla_import_cursor';
    private $lock_key = 'carmilla_import_lock';

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

        if (!$dry_run) {
            // Check lock to prevent parallel imports
            $lock = get_transient($this->lock_key);
            if ($lock) {
                return new \WP_Error('import_locked', 'Another import is currently running.');
            }
            set_transient($this->lock_key, time(), 300); // 5 min lock
        }

        $state = get_option($this->state_key, []);
        $cursor = $dry_run ? 0 : (int) get_option($this->cursor_key, 0);
        
        $results = [
            'create' => 0,
            'update' => 0,
            'skip' => 0,
            'conflict' => 0
        ];

        $chunks = $this->manifest['chunks'];
        $total_chunks = count($chunks);

        for ($i = $cursor; $i < $total_chunks; $i++) {
            $chunk = $chunks[$i];
            $feature = $chunk['feature'];
            $checksum = $chunk['checksum'] ?? md5(json_encode($chunk));

            // Skip if feature is not active/licensed, or dependencies are missing
            if (!$this->is_feature_active($feature, $chunk)) {
                $results['skip']++;
                if (!$dry_run) update_option($this->cursor_key, $i + 1);
                continue;
            }

            // Idempotency: Skip if already imported with the same checksum
            if (isset($state[$feature]) && $state[$feature] === $checksum) {
                $results['skip']++;
                if (!$dry_run) update_option($this->cursor_key, $i + 1);
                continue;
            }

            // If state exists but checksum differs, it's an update/conflict
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
                    update_option($this->cursor_key, $i + 1);
                } else {
                    $results['conflict']++;
                    if (isset($state[$feature])) {
                        $results['update']--;
                    } else {
                        $results['create']--;
                    }
                    // Break on conflict to allow manual resolution or retry
                    break;
                }
            }
        }

        if (!$dry_run) {
            delete_transient($this->lock_key);
            if ((int) get_option($this->cursor_key, 0) >= $total_chunks) {
                delete_option($this->cursor_key); // Finished successfully
            }
        }

        return $results;
    }

    private function is_feature_active($feature, $chunk = null) {
        // Abstract check for SKU/Feature
        $active_features = apply_filters('carmilla_active_features', ['core', 'messaging', 'payment']);
        $is_active = in_array($feature, $active_features, true);

        if (!$is_active) {
            return false;
        }

        // Check chunk dependencies
        if ($chunk && !empty($chunk['dependencies'])) {
            foreach ((array) $chunk['dependencies'] as $dep) {
                if (!in_array($dep, $active_features, true)) {
                    return false;
                }
            }
        }

        return true;
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
            case 'wp_media':
                require_once(ABSPATH . 'wp-admin/includes/media.php');
                require_once(ABSPATH . 'wp-admin/includes/file.php');
                require_once(ABSPATH . 'wp-admin/includes/image.php');

                foreach ($data as $media_data) {
                    $seed_id = $media_data['seed_id'];
                    $url = $media_data['url'];
                    
                    // Strict Allowlist and Security checks
                    $allowed_domains = apply_filters('carmilla_seed_allowed_domains', ['carmilla.local', 'assets.carmilla.com']);
                    $parsed = parse_url($url);
                    if (empty($parsed['host']) || !in_array($parsed['host'], $allowed_domains, true)) {
                        continue; // Prevent SSRF and hotlinking from unknown sources
                    }
                    
                    $existing_registry = $wpdb->get_var($wpdb->prepare(
                        "SELECT wp_entity_id FROM {$wpdb->prefix}carmilla_seed_objects WHERE seed_entity_id = %s AND wp_entity_type = 'attachment'",
                        $seed_id
                    ));

                    if (empty($existing_registry)) {
                        $attachment_id = media_sideload_image($url, 0, $media_data['title'] ?? '', 'id');
                        if (!is_wp_error($attachment_id)) {
                            update_post_meta($attachment_id, '_carmilla_seed_id', $seed_id);
                            $this->log_seed_object($pack_id, $feature, 'attachment', $attachment_id, $seed_id);
                        }
                    }
                }
                break;
            default:
                do_action('carmilla_import_chunk_' . $schema, $data, $pack_id, $feature);
                break;
        }

        return true;
    }

    public function rollback_import() {
        if (empty($this->manifest) || !isset($this->manifest['chunks'])) {
            return new \WP_Error('invalid_manifest', 'Manifest is missing or invalid.');
        }

        global $wpdb;
        $results = ['deleted' => 0, 'skipped_modified' => 0];

        foreach ($this->manifest['chunks'] as $chunk) {
            $pack_id = $chunk['pack_id'] ?? 'default_pack';
            $feature = $chunk['feature'] ?? 'core';
            $schema = $chunk['schema'] ?? '';
            $data = $chunk['data'] ?? [];

            switch ($schema) {
                case 'wp_options':
                    foreach ($data as $key => $original_value) {
                        $current_value = get_option($key);
                        if ($current_value == $original_value) {
                            delete_option($key);
                            $wpdb->delete("{$wpdb->prefix}carmilla_seed_objects", ['pack_id' => $pack_id, 'feature' => $feature, 'seed_entity_id' => $key]);
                            $results['deleted']++;
                        } else {
                            $results['skipped_modified']++;
                        }
                    }
                    break;
                case 'wp_posts':
                    foreach ($data as $post_data) {
                        $seed_id = $post_data['seed_id'];
                        $wp_entity_id = $wpdb->get_var($wpdb->prepare(
                            "SELECT wp_entity_id FROM {$wpdb->prefix}carmilla_seed_objects WHERE seed_entity_id = %s AND wp_entity_type = 'post'",
                            $seed_id
                        ));

                        if ($wp_entity_id) {
                            $post = get_post($wp_entity_id);
                            if ($post && $post->post_date === $post->post_modified) {
                                wp_delete_post($wp_entity_id, true);
                                $wpdb->delete("{$wpdb->prefix}carmilla_seed_objects", ['wp_entity_id' => $wp_entity_id]);
                                $results['deleted']++;
                            } else {
                                $results['skipped_modified']++;
                            }
                        }
                    }
                    break;
                default:
                    do_action('carmilla_rollback_chunk_' . $schema, $data, $pack_id, $feature);
                    break;
            }
        }
        
        // Reset state
        delete_option($this->state_key);
        delete_option($this->cursor_key);

        return $results;
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
