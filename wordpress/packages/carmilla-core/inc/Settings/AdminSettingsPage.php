<?php
namespace Carmilla\Core\Settings;

use Carmilla\Core\Catalog\EntitlementClaims;
use Carmilla\Core\Catalog\DependencyResolver;
use Carmilla\Core\Health\HealthChecker;

class AdminSettingsPage {

    public static function init() {
        add_action('admin_menu', [self::class, 'add_menu_page']);
    }

    public static function add_menu_page() {
        add_menu_page(
            'Carmilla Capabilities',
            'Carmilla',
            'manage_options',
            'carmilla-settings',
            [self::class, 'render_page'],
            'dashicons-superhero',
            58
        );
    }

    public static function render_page() {
        if (!current_user_can('manage_options')) {
            return;
        }

        // Get effective features from Kernel
        global $carmilla_active_features;
        $active_features = $carmilla_active_features ?? [];
        
        // Get claims to see what is bought vs overridden
        $claims = EntitlementClaims::get_fully_unlocked_fixture(); 
        // In real prod, this comes from license key validation
        
        ?>
        <div class="wrap" dir="rtl">
            <h1>تنظیمات و قابلیت‌های Carmilla</h1>
            
            <p>در این بخش می‌توانید وضعیت لایسنس و دسترسی‌های اپلیکیشن/سایت خود را مشاهده کنید.</p>

            <table class="widefat fixed striped">
                <thead>
                    <tr>
                        <th>قابلیت (Feature)</th>
                        <th>وضعیت (Status)</th>
                        <th>نیازمندی (Prerequisites)</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($active_features as $feature => $is_active) : ?>
                    <tr>
                        <td><strong><?php echo esc_html($feature); ?></strong></td>
                        <td>
                            <?php if ($is_active) : ?>
                                <span style="color: green;">فعال (Active)</span>
                            <?php else : ?>
                                <span style="color: red;">غیرفعال (Inactive)</span>
                            <?php endif; ?>
                        </td>
                        <td>
                            <?php 
                                if (strpos($feature, 'commerce') === 0 && !class_exists('WooCommerce')) {
                                    echo '<span style="color: orange;">نیازمند نصب WooCommerce</span>';
                                } else {
                                    echo 'برآورده شده';
                                }
                            ?>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>

            <hr style="margin: 24px 0;">
            <h2>پیش‌بررسی راه‌اندازی و عیب‌یابی سیستم (Preflight & Diagnostics)</h2>
            <?php
                $preflight = class_exists(HealthChecker::class) ? HealthChecker::run_preflight('auto') : null;
                $diagnostics = class_exists(HealthChecker::class) ? HealthChecker::get_diagnostics() : null;
            ?>
            <?php if ($preflight) : ?>
                <p>
                    <strong>میزبان فعال:</strong> <code><?php echo esc_html($preflight['host_context']); ?></code> |
                    <strong>وضعیت آماده‌به‌کار:</strong>
                    <?php if ($preflight['ready']) : ?>
                        <span style="color: green; font-weight: bold;">آماده (Ready)</span>
                    <?php else : ?>
                        <span style="color: red; font-weight: bold;">نیازمند اصلاح (Blocked)</span>
                    <?php endif; ?>
                    <a href="<?php echo esc_url(add_query_arg('carmilla_recheck', '1')); ?>" class="button button-secondary" style="margin-right: 12px;">بررسی مجدد (Re-run Preflight)</a>
                </p>

                <table class="widefat fixed striped">
                    <thead>
                        <tr>
                            <th>بررسی (Check)</th>
                            <th>دامنه (Scope)</th>
                            <th>وضعیت (Status)</th>
                            <th>پیام و راه رفع (Message & Remediation)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($preflight['checks'] as $chk) : ?>
                        <tr>
                            <td><strong><?php echo esc_html($chk['name']); ?></strong></td>
                            <td><code><?php echo esc_html($chk['scope'] ?? 'shared'); ?></code></td>
                            <td><code><?php echo esc_html(strtoupper($chk['status'])); ?></code></td>
                            <td>
                                <?php echo esc_html($chk['message']); ?>
                                <?php if (!empty($chk['remediation'])) : ?>
                                    <br><small style="color: #b32d2e;"><strong>راه رفع:</strong> <?php echo esc_html($chk['remediation']); ?></small>
                                <?php endif; ?>
                            </td>
                        </tr>
                        <?php endforeach; ?>
                    </tbody>
                </table>

                <?php if ($diagnostics) : ?>
                    <details style="margin-top: 16px;">
                        <summary style="cursor: pointer; font-weight: bold;">گزارش تشخیصی بدون اطلاعات محرمانه (Redacted Diagnostics JSON)</summary>
                        <pre dir="ltr" style="background: #f6f7f7; padding: 12px; border: 1px solid #dcdcde; overflow: auto; max-height: 300px;"><?php echo esc_html(wp_json_encode($diagnostics, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE)); ?></pre>
                    </details>
                <?php endif; ?>
            <?php endif; ?>
        </div>
        <?php
    }
}
