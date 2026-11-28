<?php
namespace Carmilla\Core\Content;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\BlockMapper', false ) ) {

/**
 * Two-way mapping between the app's BlogBlockDto array and Gutenberg block markup.
 *
 * App block: { type, content, level?, url?, items? }
 *   type in: header, paragraph, image, button, list, quote, divider
 *
 * Part of Task P04-WORDPRESS-CODE-026.
 */
class BlockMapper {

	/**
	 * Parse a post's content into an array of BlogBlockDto-shaped blocks.
	 *
	 * @param \WP_Post $post
	 * @return array<int, array<string, mixed>>
	 */
	public static function post_to_blocks( \WP_Post $post ): array {
		$blocks = function_exists( 'parse_blocks' ) ? parse_blocks( $post->post_content ) : [];
		$out    = [];

		foreach ( $blocks as $block ) {
			$mapped = self::map_block( $block );
			if ( null !== $mapped ) {
				$out[] = $mapped;
			}
		}

		if ( empty( $out ) && '' !== trim( wp_strip_all_tags( $post->post_content ) ) ) {
			$out[] = [
				'type'    => 'paragraph',
				'content' => wp_strip_all_tags( $post->post_content ),
			];
		}

		return $out;
	}

	/**
	 * @param array<string, mixed> $block
	 * @return array<string, mixed>|null
	 */
	private static function map_block( array $block ): ?array {
		$name  = $block['blockName'] ?? null;
		$attrs = $block['attrs'] ?? [];
		$html  = trim( (string) ( $block['innerHTML'] ?? '' ) );

		switch ( $name ) {
			case 'core/heading':
				$level = isset( $attrs['level'] ) ? (int) $attrs['level'] : self::detect_heading_level( $html );
				return [
					'type'    => 'header',
					'content' => wp_strip_all_tags( $html ),
					'level'   => max( 1, min( 3, $level ) ),
				];

			case 'core/paragraph':
				$text = wp_strip_all_tags( $html );
				return '' === $text ? null : [ 'type' => 'paragraph', 'content' => $text ];

			case 'core/image':
				$src = self::first_attr( $html, 'img', 'src' );
				if ( ! $src && isset( $attrs['url'] ) ) {
					$src = $attrs['url'];
				}
				return $src ? [ 'type' => 'image', 'content' => $src, 'url' => $src ] : null;

			case 'core/list':
				$items = self::extract_list_items( self::inner_html( $block ) );
				return [ 'type' => 'list', 'content' => '', 'items' => $items ];

			case 'core/quote':
				return [ 'type' => 'quote', 'content' => wp_strip_all_tags( self::inner_html( $block ) ) ];

			case 'core/separator':
				return [ 'type' => 'divider', 'content' => '' ];

			case 'core/buttons':
			case 'core/button':
				$inner = self::inner_html( $block );
				$label = wp_strip_all_tags( $inner );
				$href  = self::first_attr( $inner, 'a', 'href' );
				return [ 'type' => 'button', 'content' => $label, 'url' => $href ?: '' ];

			default:
				$text = wp_strip_all_tags( self::inner_html( $block ) );
				return '' === $text ? null : [ 'type' => 'paragraph', 'content' => $text ];
		}
	}

	/**
	 * Serialize an array of BlogBlockDto blocks into Gutenberg block markup for post_content.
	 *
	 * @param array<int, array<string, mixed>> $blocks
	 */
	public static function blocks_to_html( array $blocks ): string {
		$parts = [];

		foreach ( $blocks as $b ) {
			$type    = $b['type'] ?? 'paragraph';
			$content = isset( $b['content'] ) ? (string) $b['content'] : '';
			$safe    = esc_html( $content );

			switch ( $type ) {
				case 'header':
					$level   = isset( $b['level'] ) ? max( 1, min( 3, (int) $b['level'] ) ) : 2;
					$parts[] = "<!-- wp:heading {\"level\":$level} -->\n<h$level>$safe</h$level>\n<!-- /wp:heading -->";
					break;

				case 'image':
					$url     = esc_url( $b['url'] ?? $content );
					$parts[] = "<!-- wp:image -->\n<figure class=\"wp-block-image\"><img src=\"$url\" alt=\"\"/></figure>\n<!-- /wp:image -->";
					break;

				case 'button':
					$url     = esc_url( $b['url'] ?? '' );
					$parts[] = "<!-- wp:buttons -->\n<div class=\"wp-block-buttons\"><!-- wp:button -->\n<div class=\"wp-block-button\"><a class=\"wp-block-button__link wp-element-button\" href=\"$url\">$safe</a></div>\n<!-- /wp:button --></div>\n<!-- /wp:buttons -->";
					break;

				case 'list':
					$items = isset( $b['items'] ) && is_array( $b['items'] ) ? $b['items'] : [];
					$li    = '';
					foreach ( $items as $item ) {
						$li .= '<li>' . esc_html( (string) $item ) . '</li>';
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

	/**
	 * @param array<string, mixed> $block
	 */
	private static function inner_html( array $block ): string {
		$html = (string) ( $block['innerHTML'] ?? '' );
		if ( ! empty( $block['innerBlocks'] ) && is_array( $block['innerBlocks'] ) ) {
			foreach ( $block['innerBlocks'] as $sub ) {
				$html .= ' ' . self::inner_html( $sub );
			}
		}
		return $html;
	}

	private static function detect_heading_level( string $html ): int {
		if ( preg_match( '/<h([1-6])\b/i', $html, $m ) ) {
			return (int) $m[1];
		}
		return 2;
	}

	private static function first_attr( string $html, string $tag, string $attr ): ?string {
		if ( preg_match( '/<' . preg_quote( $tag, '/' ) . '[^>]*\b' . preg_quote( $attr, '/' ) . '=["\']([^"\']+)["\']/i', $html, $m ) ) {
			return html_entity_decode( $m[1], ENT_QUOTES, 'UTF-8' );
		}
		return null;
	}

	/**
	 * @return array<int, string>
	 */
	private static function extract_list_items( string $html ): array {
		if ( preg_match_all( '/<li[^>]*>(.*?)<\/li>/is', $html, $m ) ) {
			return array_values(
				array_filter(
					array_map(
						static function ( string $s ): string {
							return trim( wp_strip_all_tags( $s ) );
						},
						$m[1]
					),
					static function ( string $s ): bool {
						return '' !== $s;
					}
				)
			);
		}
		$fallback = trim( wp_strip_all_tags( $html ) );
		return '' !== $fallback ? [ $fallback ] : [];
	}
}

}
