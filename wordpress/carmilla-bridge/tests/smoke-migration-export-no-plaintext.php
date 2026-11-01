<?php
namespace {
    define( 'ABSPATH', __DIR__ . '/' );
    define( 'DAY_IN_SECONDS', 86400 );
    class WP_CLI {
        public static $successes = [];
        public static function error( $message ) { throw new \RuntimeException( $message ); }
        public static function success( $message ) { self::$successes[] = $message; }
    }
    class Synthetic_Wpdb {
        public $posts = 'wp_posts';
        public $postmeta = 'wp_postmeta';
        public function prepare( $query, $id ) { return str_replace( '%d', (string) $id, $query ); }
        public function get_results( $query ) {
            if ( false !== strpos( $query, 'post_type NOT IN' ) ) {
                return [ (object) [ 'ID' => 17, 'post_title' => 'Synthetic post', 'post_content' => 'Synthetic content', 'post_type' => 'post', 'post_status' => 'publish', 'post_parent' => 0 ] ];
            }
            if ( false !== strpos( $query, "post_type = 'attachment'" ) ) {
                return [ (object) [ 'ID' => 18, 'post_title' => 'Synthetic media', 'guid' => 'https://example.test/image.png' ] ];
            }
            return [ (object) [ 'meta_key' => 'safe_label', 'meta_value' => 'Synthetic' ] ];
        }
    }
    $wpdb = new Synthetic_Wpdb();
    function wp_mkdir_p( $directory ) { return is_dir( $directory ) || mkdir( $directory, 0700, true ); }
    function trailingslashit( $path ) { return rtrim( $path, '/' ) . '/'; }
    function esc_sql( $value ) { return addslashes( $value ); }
    function wp_json_encode( $value, $flags = 0 ) { return json_encode( $value, $flags ); }
    function get_attached_file() { return false; }
}
namespace Carmilla\Bridge\Migration {
    // Inject one write failure after the first encrypted temp file was written.
    function file_put_contents( $filename, $data, $flags = 0 ) {
        if ( ! empty( $GLOBALS['fail_media_write'] ) && false !== strpos( $filename, 'media.enc.' ) ) {
            return false;
        }
        return \file_put_contents( $filename, $data, $flags );
    }
}
namespace {
    require_once __DIR__ . '/../includes/Migration/Migration_Config.php';
    require_once __DIR__ . '/../includes/Migration/Migration_Crypto.php';
    require_once __DIR__ . '/../includes/Migration/Export_Command.php';
    $root = sys_get_temp_dir() . '/carmilla-export-smoke-' . bin2hex( random_bytes( 8 ) );
    $key = str_repeat( 'k', 32 ); // Synthetic fixture, never a production key.
    $uuid = 'synthetic-site';
    $good = $root . '/success';
    $failed = $root . '/failure';
    $legacy = $root . '/legacy';
    try {
        \Carmilla\Bridge\Migration\Export_Command::execute( [], [ 'output' => $good, 'customer_uuid' => $uuid, 'encryption_key' => $key ] );
        $posts = \Carmilla\Bridge\Migration\Migration_Crypto::decrypt_payload( file_get_contents( $good . '/posts.enc' ), $key, $uuid );
        $media = \Carmilla\Bridge\Migration\Migration_Crypto::decrypt_payload( file_get_contents( $good . '/media.enc' ), $key, $uuid );
        $success = 1 === count( WP_CLI::$successes ) && false !== strpos( $posts, 'Synthetic post' )
            && false !== strpos( $media, 'Synthetic media' )
            && ! file_exists( $good . '/posts.ndjson' ) && ! file_exists( $good . '/media.ndjson' )
            && 2 === count( glob( $good . '/*' ) );
        $GLOBALS['fail_media_write'] = true;
        $failure_reported = false;
        try {
            \Carmilla\Bridge\Migration\Export_Command::execute( [], [ 'output' => $failed, 'customer_uuid' => $uuid, 'encryption_key' => $key ] );
        } catch ( \RuntimeException $exception ) {
            $failure_reported = false !== strpos( $exception->getMessage(), 'Encrypted migration export failed' );
        }
        $failure = $failure_reported && 1 === count( WP_CLI::$successes )
            && ! file_exists( $failed . '/posts.ndjson' ) && ! file_exists( $failed . '/media.ndjson' )
            && [] === glob( $failed . '/*' );
        mkdir( $legacy, 0700, true );
        file_put_contents( $legacy . '/posts.ndjson', "synthetic legacy plaintext\n" );
        $legacy_rejected = false;
        try {
            \Carmilla\Bridge\Migration\Export_Command::execute( [], [ 'output' => $legacy, 'customer_uuid' => $uuid, 'encryption_key' => $key ] );
        } catch ( \RuntimeException $exception ) {
            $legacy_rejected = false !== strpos( $exception->getMessage(), 'legacy plaintext export exists' );
        }
        $legacy_rejected = $legacy_rejected && 1 === count( WP_CLI::$successes ) && ! file_exists( $legacy . '/posts.enc' );
        echo $success ? "PASS: encrypted export and decrypt\n" : "FAIL: encrypted export and decrypt\n";
        echo $failure ? "PASS: write failure cleans encrypted temp without plaintext\n" : "FAIL: write failure cleanup\n";
        echo $legacy_rejected ? "PASS: legacy plaintext requires operator cleanup\n" : "FAIL: legacy plaintext was ignored\n";
        $result = $success && $failure && $legacy_rejected ? 0 : 1;
    } finally {
        foreach ( [ $good, $failed, $legacy ] as $directory ) {
            foreach ( glob( $directory . '/*' ) ?: [] as $file ) { unlink( $file ); }
            if ( is_dir( $directory ) ) { rmdir( $directory ); }
        }
        if ( is_dir( $root ) ) { rmdir( $root ); }
    }
    exit( $result );
}
