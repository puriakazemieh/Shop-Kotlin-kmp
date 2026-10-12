<?php
if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class CB_Integrations_Controller {

    public function init() {
        add_action('admin_menu', [$this, 'register_admin_page']);
        add_action('admin_post_carmilla_test_message', [$this, 'handle_test_message']);
    }

    public function handle_test_message() {
        if (!current_user_can('manage_carmilla_integrations')) {
            wp_die(__('You do not have sufficient permissions to access this page.'));
        }

        check_admin_referer('carmilla_test_message', 'carmilla_test_message_nonce');

        $type = isset($_POST['test_type']) ? sanitize_text_field($_POST['test_type']) : '';
        $recipient = isset($_POST['test_recipient']) ? sanitize_text_field($_POST['test_recipient']) : '';
        $body = isset($_POST['test_body']) ? sanitize_textarea_field($_POST['test_body']) : '';

        if (empty($recipient) || empty($body)) {
            wp_die(__('Recipient and message are required.', 'carmilla-bridge'));
        }

        try {
            if ($type === 'sms') {
                $result = CB_SMS_HTTP_Adapter::send($recipient, $body);
                if (is_wp_error($result)) {
                    throw new Exception($result->get_error_message());
                }
            } elseif ($type === 'email') {
                $result = CB_Mail_Adapter::send($recipient, 'Test Message', $body);
                if ($result === false) {
                    throw new Exception('Email sending failed.');
                }
            }
            
            // Redact recipient for logging
            $redacted_recipient = substr($recipient, 0, 3) . '***' . substr($recipient, -2);
            error_log("Test $type message sent successfully to $redacted_recipient");
            
            wp_redirect(add_query_arg(['page' => 'carmilla-integrations', 'tab' => 'test', 'status' => 'success'], admin_url('admin.php')));
            exit;
        } catch (Exception $e) {
            error_log("Test $type message failed: " . $e->getMessage());
            wp_die(__('Failed to send test message: ', 'carmilla-bridge') . esc_html($e->getMessage()));
        }
    }

    public function register_admin_page() {
        // Capability
        $capability = 'manage_carmilla_integrations';

        // Add top-level menu if it doesn't exist
        add_menu_page(
            __('Carmilla', 'carmilla-bridge'),
            __('Carmilla', 'carmilla-bridge'),
            $capability,
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
            $capability,
            'carmilla-integrations',
            [$this, 'render_admin_page']
        );
    }

    public function render_admin_page() {
        if ( ! current_user_can('manage_carmilla_integrations') ) {
            return;
        }
        $active_tab = isset( $_GET['tab'] ) ? sanitize_text_field( $_GET['tab'] ) : 'sms';
        ?>
        <div class="wrap">
            <h1><?php esc_html_e('Carmilla Integrations', 'carmilla-bridge'); ?></h1>
            
            <h2 class="nav-tab-wrapper">
                <a href="?page=carmilla-integrations&tab=sms" class="nav-tab <?php echo $active_tab === 'sms' ? 'nav-tab-active' : ''; ?>">SMS</a>
                <a href="?page=carmilla-integrations&tab=email" class="nav-tab <?php echo $active_tab === 'email' ? 'nav-tab-active' : ''; ?>">Email</a>
                <a href="?page=carmilla-integrations&tab=templates" class="nav-tab <?php echo $active_tab === 'templates' ? 'nav-tab-active' : ''; ?>">Templates</a>
                <a href="?page=carmilla-integrations&tab=health" class="nav-tab <?php echo $active_tab === 'health' ? 'nav-tab-active' : ''; ?>">Health</a>
                <a href="?page=carmilla-integrations&tab=test" class="nav-tab <?php echo $active_tab === 'test' ? 'nav-tab-active' : ''; ?>">Test</a>
            </h2>

            <div class="tab-content">
                <?php
                if ($active_tab === 'sms') {
                    $this->render_settings_form('sms');
                } elseif ($active_tab === 'email') {
                    $this->render_settings_form('email');
                } elseif ($active_tab === 'templates') {
                    $this->render_settings_form('templates');
                } elseif ($active_tab === 'health') {
                    $this->render_health();
                } elseif ($active_tab === 'test') {
                    ?>
                    <h3>Test Messages</h3>
                    <form method="post" action="<?php echo esc_url(admin_url('admin-post.php')); ?>">
                        <?php wp_nonce_field('carmilla_test_message', 'carmilla_test_message_nonce'); ?>
                        <input type="hidden" name="action" value="carmilla_test_message">
                        
                        <table class="form-table">
                            <tr>
                                <th scope="row"><label for="test_type">Type</label></th>
                                <td>
                                    <select name="test_type" id="test_type">
                                        <option value="sms">SMS</option>
                                        <option value="email">Email</option>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row"><label for="test_recipient">Recipient</label></th>
                                <td><input name="test_recipient" type="text" id="test_recipient" class="regular-text" required></td>
                            </tr>
                            <tr>
                                <th scope="row"><label for="test_body">Message</label></th>
                                <td><textarea name="test_body" id="test_body" class="large-text" rows="3" required></textarea></td>
                            </tr>
                        </table>
                        
                        <p class="submit">
                            <input type="submit" name="submit" id="submit" class="button button-primary" value="Send Test">
                        </p>
                    </form>
                    <?php
                }
                ?>
            </div>
        </div>
        <?php
    }

    private function render_settings_form($section) {
        $settings = get_option('carmilla_message_settings', []);
        $value = static function ($key) use ($settings) { return $settings[$key] ?? ''; };
        settings_errors('carmilla_message_settings');
        ?>
        <form method="post" action="options.php">
            <?php settings_fields('carmilla_options'); ?>
            <table class="form-table" role="presentation">
            <?php if ($section === 'sms') : ?>
                <tr><th><label for="sms_provider">Provider</label></th><td><input class="regular-text" id="sms_provider" name="carmilla_message_settings[sms_provider]" value="<?php echo esc_attr($value('sms_provider')); ?>"></td></tr>
                <tr><th><label for="sms_api_url">HTTPS API URL</label></th><td><input class="regular-text code" type="url" id="sms_api_url" name="carmilla_message_settings[sms_api_url]" value="<?php echo esc_attr($value('sms_api_url')); ?>" placeholder="https://provider.example/send"></td></tr>
                <tr><th><label for="sms_api_key">API key</label></th><td><input class="regular-text" type="password" autocomplete="new-password" id="sms_api_key" name="carmilla_message_settings[sms_api_key]" value=""><p class="description">Leave blank to keep the existing key.</p></td></tr>
                <tr><th><label for="sms_success_code">Success code</label></th><td><input class="small-text" type="number" id="sms_success_code" name="carmilla_message_settings[sms_success_code]" value="<?php echo esc_attr($value('sms_success_code') ?: '200'); ?>"></td></tr>
            <?php elseif ($section === 'email') : ?>
                <tr><th><label for="smtp_host">SMTP host</label></th><td><input class="regular-text" id="smtp_host" name="carmilla_message_settings[smtp_host]" value="<?php echo esc_attr($value('smtp_host')); ?>"></td></tr>
                <tr><th><label for="smtp_port">Port</label></th><td><input class="small-text" type="number" id="smtp_port" name="carmilla_message_settings[smtp_port]" value="<?php echo esc_attr($value('smtp_port') ?: '587'); ?>"></td></tr>
                <tr><th><label for="smtp_user">Username</label></th><td><input class="regular-text" id="smtp_user" name="carmilla_message_settings[smtp_user]" value="<?php echo esc_attr($value('smtp_user')); ?>"></td></tr>
                <tr><th><label for="smtp_pass">Password</label></th><td><input class="regular-text" type="password" autocomplete="new-password" id="smtp_pass" name="carmilla_message_settings[smtp_pass]" value=""><p class="description">Leave blank to keep the existing password.</p></td></tr>
                <tr><th><label for="email_from">From email</label></th><td><input class="regular-text" type="email" id="email_from" name="carmilla_message_settings[email_from]" value="<?php echo esc_attr($value('email_from')); ?>"></td></tr>
            <?php else : ?>
                <tr><th><label for="sms_template">SMS template</label></th><td><textarea class="large-text" rows="4" id="sms_template" name="carmilla_message_settings[sms_template]"><?php echo esc_textarea($value('sms_template')); ?></textarea></td></tr>
                <tr><th><label for="email_template">Email template</label></th><td><textarea class="large-text" rows="6" id="email_template" name="carmilla_message_settings[email_template]"><?php echo esc_textarea($value('email_template')); ?></textarea></td></tr>
            <?php endif; ?>
            </table>
            <?php submit_button(__('Save settings', 'carmilla-bridge')); ?>
        </form>
        <?php
    }

    private function render_health() {
        $settings = get_option('carmilla_message_settings', []);
        $checks = [
            'SMS provider' => !empty($settings['sms_api_url']),
            'Email transport' => !empty($settings['smtp_host']) || function_exists('wp_mail'),
        ];
        echo '<h3>Configuration health</h3><table class="widefat striped"><tbody>';
        foreach ($checks as $label => $ok) {
            echo '<tr><td>' . esc_html($label) . '</td><td>' . ($ok ? 'Ready' : 'Needs configuration') . '</td></tr>';
        }
        echo '</tbody></table>';
    }
}
