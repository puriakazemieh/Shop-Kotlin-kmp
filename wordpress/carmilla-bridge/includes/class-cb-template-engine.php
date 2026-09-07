<?php
if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class CB_Template_Engine {

    /**
     * Renders a string template by replacing {{key}} with values from $context.
     * 
     * @param string $template The template string.
     * @param array $context The variables to inject.
     * @param array $allowlist Array of allowed keys. If empty, all keys are allowed.
     * @param bool $is_html Whether to sanitize as HTML and wrap in RTL.
     * @return string Rendered template.
     */
    public static function render($template, $context = [], $allowlist = [], $is_html = false) {
        if (empty($template)) {
            return '';
        }

        $replacements = [];
        foreach ($context as $key => $value) {
            // Check allowlist
            if (!empty($allowlist) && !in_array($key, $allowlist, true)) {
                continue;
            }

            // Sanitize value
            $safe_value = $is_html ? esc_html($value) : sanitize_text_field($value);
            $replacements['{{' . $key . '}}'] = $safe_value;
        }

        $rendered = strtr($template, $replacements);

        // For HTML emails or previews, enforce RTL structure safely
        if ($is_html) {
            $rendered = wp_kses_post($rendered);
            $rendered = '<div dir="rtl" style="text-align: right; font-family: Tahoma, Arial, sans-serif;">' . $rendered . '</div>';
        }

        return $rendered;
    }
}
