<?php
if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class CB_Integrations_Controller {

    public function init() {
        add_action('admin_menu', [\, 'register_admin_page']);
    }

    public function register_admin_page() {
        // Capability
        \ = 'manage_carmilla_integrations';

        // Add top-level menu if it doesn't exist
        add_menu_page(
            __('Carmilla', 'carmilla-bridge'),
            __('Carmilla', 'carmilla-bridge'),
            \,
            'carmilla',
            '',
            'dashicons-store',
            58
        );

        // Add Integrations submenu
        add_submenu_page(
            'carmilla',
            __('Integrations', 'carmilla-bridge'),
            __('Integrations', 'carmilla-bridge'),
            \,
            'carmilla-integrations',
            [\, 'render_admin_page']
        );
    }

    public function render_admin_page() {
        if ( ! current_user_can('manage_carmilla_integrations') ) {
            return;
        }
        \ = isset( \['tab'] ) ? sanitize_text_field( \['tab'] ) : 'sms';
        ?>
        <div class="wrap">
            <h1><?php esc_html_e('Carmilla Integrations', 'carmilla-bridge'); ?></h1>
            
            <h2 class="nav-tab-wrapper">
                <a href="?page=carmilla-integrations&tab=sms" class="nav-tab <?php echo \ === 'sms' ? 'nav-tab-active' : ''; ?>">SMS</a>
                <a href="?page=carmilla-integrations&tab=email" class="nav-tab <?php echo \ === 'email' ? 'nav-tab-active' : ''; ?>">Email</a>
                <a href="?page=carmilla-integrations&tab=templates" class="nav-tab <?php echo \ === 'templates' ? 'nav-tab-active' : ''; ?>">Templates</a>
                <a href="?page=carmilla-integrations&tab=health" class="nav-tab <?php echo \ === 'health' ? 'nav-tab-active' : ''; ?>">Health</a>
                <a href="?page=carmilla-integrations&tab=test" class="nav-tab <?php echo \ === 'test' ? 'nav-tab-active' : ''; ?>">Test</a>
            </h2>

            <div class="tab-content">
                <?php
                if (\ === 'sms') {
                    echo '<h3>SMS Settings</h3><p>Manage SMS providers.</p>';
                } elseif (\ === 'email') {
                    echo '<h3>Email Settings</h3><p>Manage Email providers.</p>';
                } elseif (\ === 'templates') {
                    echo '<h3>Templates</h3><p>Manage message templates.</p>';
                } elseif (\ === 'health') {
                    echo '<h3>Health Check</h3><p>Check the status of integrations.</p>';
                } elseif (\ === 'test') {
                    echo '<h3>Test Messages</h3><p>Send a test message.</p>';
                }
                ?>
            </div>
        </div>
        <?php
    }
}
