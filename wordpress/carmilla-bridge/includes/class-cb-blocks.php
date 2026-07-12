<?php
/**
 * Two-way mapping between the app's BlogBlockDto array and Gutenberg block markup.
 *
 * App block:  { type, content, level?, url?, items? }
 *   type in: header, paragraph, image, button, list, quote, divider
 * WP stores the article body as Gutenberg blocks (parse_blocks / serialized comments).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Blocks {

	/**
	 * Parse a post's content into an array of BlogBlockDto-shaped blocks.
	 *
	 * @return array<int,array>
	 */
	public static function post_to_blocks( WP_Post $post ): array {
		$blocks = parse_blocks( $post->post_content );
		$out    = array();

		foreach ( $blocks as $block ) {
			$mapped = self::map_block( $block );
			if ( $mapped !== null ) {
				$out[] = $mapped;
			}
		}
		// Fallback: classic (non-block) content becomes a single paragraph.
		if ( empty( $out ) && trim( wp_strip_all_tags( $post->post_content ) ) !== '' ) {
			$out[] = array( 'type' => 'paragraph', 'content' => wp_strip_all_tags( $post->post_content ) );
		}
		return $out;
	}

	private static function map_block( array $block ): ?array {
		$name  = $block['blockName'] ?? null;
		$attrs = $block['attrs'] ?? array();
		$html  = trim( $block['innerHTML'] ?? '' );

		switch ( $name ) {
			case 'core/heading':
				$level = isset( $attrs['level'] ) ? (int) $attrs['level'] : self::detect_heading_level( $html );
				return array(
					'type'    => 'header',
					'content' => wp_strip_all_tags( $html ),
					'level'   => max( 1, min( 3, $level ) ),
				);

			case 'core/paragraph':
				$text = wp_strip_all_tags( $html );
				return $text === '' ? null : array( 'type' => 'paragraph', 'content' => $text );

			case 'core/image':
				$src = self::first_attr( $html, 'img', 'src' );
				if ( ! $src && isset( $attrs['url'] ) ) {
					$src = $attrs['url'];
				}
				return $src ? array( 'type' => 'image', 'content' => $src, 'url' => $src ) : null;

			case 'core/list':
				$items = self::extract_list_items( self::inner_html( $block ) );
				return array( 'type' => 'list', 'content' => '', 'items' => $items );

			case 'core/quote':
				return array( 'type' => 'quote', 'content' => wp_strip_all_tags( self::inner_html( $block ) ) );

			case 'core/separator':
				return array( 'type' => 'divider', 'content' => '' );

			case 'core/buttons':
			case 'core/button':
				$inner = self::inner_html( $block );
				$label = wp_strip_all_tags( $inner );
				$href  = self::first_attr( $inner, 'a', 'href' );
				return array( 'type' => 'button', 'content' => $label, 'url' => $href ?: '' );

			default:
				// Skip container/unknown blocks but keep any raw text they carry.
				$text = wp_strip_all_tags( self::inner_html( $block ) );
				return $text === '' ? null : array( 'type' => 'paragraph', 'content' => $text );
		}
	}

	/**
	 * Serialize an array of BlogBlockDto blocks into Gutenberg block markup for post_content.
	 *
	 * @param array<int,array> $blocks
	 */
	public static function blocks_to_html( array $blocks ): string {
		$parts = array();

		foreach ( $blocks as $b ) {
			$type    = $b['type'] ?? 'paragraph';
			$content = isset( $b['content'] ) ? (string) $b['content'] : '';
			$safe    = esc_html( $content );

			switch ( $type ) {
				case 'header':
					$level = isset( $b['level'] ) ? max( 1, min( 3, (int) $b['level'] ) ) : 2;
					$parts[] = "<!-- wp:heading {\"level\":$level} -->\n<h$level>$safe</h$level>\n<!-- /wp:heading -->";
					break;

				case 'image':
					$url = esc_url( $b['url'] ?? $content );
					$parts[] = "<!-- wp:image -->\n<figure class=\"wp-block-image\"><img src=\"$url\" alt=\"\"/></figure>\n<!-- /wp:image -->";
					break;

				case 'button':
					$url = esc_url( $b['url'] ?? '' );
					$parts[] = "<!-- wp:buttons -->\n<div class=\"wp-block-buttons\"><!-- wp:button -->\n<div class=\"wp-block-button\"><a class=\"wp-block-button__link wp-element-button\" href=\"$url\">$safe</a></div>\n<!-- /wp:button --></div>\n<!-- /wp:buttons -->";
					break;

				case 'list':
					$items = isset( $b['items'] ) && is_array( $b['items'] ) ? $b['items'] : array();
					$li    = '';
					foreach ( $items as $item ) {
						$li .= '<li>' . esc_html( $item ) . '</li>';
					}
					$parts[] = "<!-- wp:list -->\n<ul>$li</ul>\n<!-- /wp:list -->";
					break;

				case 'quote':
					$parts[] = "<!-- wp:quote -->\n<blockquote class=\"wp-block-quote\"><p>$safe</p></blockquote>\n<!-- /wp:quote -->";
					break;

				case 'divider':
					$parts[] = "<!-- wp:separator -->\n<hr class=\"wp-block-separator has-alpha-channel-opacity\"/>\n<!-- /wp:separator -->";
					break;

				case 'paragraph':
				default:
					$parts[] = "<!-- wp:paragraph -->\n<p>$safe</p>\n<!-- /wp:paragraph -->";
					break;
			}
		}

		return implode( "\n\n", $parts );
	}

	// ---- small HTML helpers -------------------------------------------------

	private static function inner_html( array $block ): string {
		if ( ! empty( $block['innerHTML'] ) ) {
			return $block['innerHTML'];
		}
		$html = '';
		foreach ( ( $block['innerBlocks'] ?? array() ) as $inner ) {
			$html .= self::inner_html( $inner );
		}
		return $html;
	}

	private static function detect_heading_level( string $html ): int {
		if ( preg_match( '/<h([1-6])/i', $html, $m ) ) {
			return (int) $m[1];
		}
		return 2;
	}

	private static function first_attr( string $html, string $tag, string $attr ): ?string {
		if ( preg_match( '/<' . preg_quote( $tag, '/' ) . '[^>]*\b' . preg_quote( $attr, '/' ) . '=["\']([^"\']+)["\']/i', $html, $m ) ) {
			return $m[1];
		}
		return null;
	}

	private static function extract_list_items( string $html ): array {
		$items = array();
		if ( preg_match_all( '/<li[^>]*>(.*?)<\/li>/is', $html, $m ) ) {
			foreach ( $m[1] as $item ) {
				$items[] = trim( wp_strip_all_tags( $item ) );
			}
		}
		return $items;
	}
}
