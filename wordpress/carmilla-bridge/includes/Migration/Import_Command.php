<?php
namespace Carmilla\Bridge\Migration;

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class Import_Command {

    public static function execute( \, \ ) {
        if (empty(\['consent']) || \['consent'] !== 'yes') {
            \WP_CLI::error("Migration handles LIVE CUSTOMER DATA. You must explicitly pass --consent=yes and ensure you have a database backup.");
            return;
        }

        \ = \['input'] ?? wp_upload_dir()['basedir'] . '/carmilla-export/posts.ndjson';
        \ = \['site_uuid'] ?? 'default-legacy-site';

        if (!file_exists(\)) {
            \WP_CLI::error("Input file not found: \");
            return;
        }

        \ = new Migrator();
        \ = [];
        
        \ = fopen(\, 'r');
        while ((\ = fgets(\)) !== false) {
            \ = json_decode(trim(\), true);
            if (\) \[] = \;
        }
        fclose(\);

        \WP_CLI::log("Pass 1: Importing " . count(\) . " records...");
        \->process_pass_one(\, \);

        \WP_CLI::log("Pass 2: Resolving relations...");
        \->process_pass_two(\, \);

        \WP_CLI::success("Migration completed successfully for site: \");
    }
}

if ( defined( 'WP_CLI' ) && WP_CLI ) {
    \WP_CLI::add_command( 'carmilla import-legacy', [ 'Carmilla\Bridge\Migration\Import_Command', 'execute' ] );
}
