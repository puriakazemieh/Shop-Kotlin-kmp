<?php
namespace Carmilla\Bridge\Migration;

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class Import_Command {

    public static function execute( $args, $assoc_args ): void {
        if ( 'yes' !== ( $assoc_args['consent'] ?? '' ) ) {
            \WP_CLI::error( 'Migration handles customer data. Pass --consent=yes only after a database and wp-content backup.' );
            return;
        }

        $input_file     = $assoc_args['input'] ?? wp_upload_dir()['basedir'] . '/carmilla-export/posts.enc';
        $site_uuid      = $assoc_args['site_uuid'] ?? '';
        $encryption_key = $assoc_args['encryption_key'] ?? '';
        $legacy_domain  = $assoc_args['legacy_domain'] ?? '';
        $new_domain     = $assoc_args['new_domain'] ?? '';
        if ( '' === $site_uuid || '' === $encryption_key ) {
            \WP_CLI::error( 'A site UUID and a 32-byte encryption key are required for import.' );
            return;
        }
        if ( ! is_readable( $input_file ) ) {
            \WP_CLI::error( "Input file is not readable: {$input_file}" );
            return;
        }

        try {
            $payload = Migration_Crypto::decrypt_payload( (string) file_get_contents( $input_file ), $encryption_key, $site_uuid );
            $records = self::decode_ndjson( $payload );
        } catch ( \Throwable $exception ) {
            \WP_CLI::error( 'Migration package verification failed: ' . $exception->getMessage() );
            return;
        }

        $migrator = new Migrator();
        $results = $migrator->process_pass_one( $records, $site_uuid, $legacy_domain, $new_domain );
        $migrator->process_pass_two( $records, $site_uuid );
        \WP_CLI::success(
            sprintf(
                'Migration completed for %s: %d inserted, %d updated, %d unchanged, %d rejected.',
                $site_uuid,
                $results['inserted'],
                $results['updated'],
                $results['skipped'],
                $results['rejected']
            )
        );
    }

    private static function decode_ndjson( string $payload ): array {
        $records = array();
        foreach ( preg_split( '/\r\n|\r|\n/', trim( $payload ) ) as $line_number => $line ) {
            if ( '' === trim( $line ) ) {
                continue;
            }
            $record = json_decode( $line, true );
            if ( ! is_array( $record ) ) {
                throw new \RuntimeException( 'Invalid JSON record at line ' . ( $line_number + 1 ) . '.' );
            }
            $records[] = $record;
        }

        return $records;
    }
}

if ( defined( 'WP_CLI' ) && WP_CLI ) {
    \WP_CLI::add_command( 'carmilla import-legacy', array( Import_Command::class, 'execute' ) );
}
