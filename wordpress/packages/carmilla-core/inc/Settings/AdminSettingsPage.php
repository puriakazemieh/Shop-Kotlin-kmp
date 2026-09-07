<?php
namespace Carmilla\Core\Settings;

use Carmilla\Core\Catalog\EntitlementClaims;
use Carmilla\Core\Catalog\DependencyResolver;

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
        </div>
        <?php
    }
}
