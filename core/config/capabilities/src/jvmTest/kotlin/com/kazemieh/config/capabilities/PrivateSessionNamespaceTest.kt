package com.kazemieh.config.capabilities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class PrivateSessionNamespaceTest {
    @Test
    fun changesFingerprintWhenTenantBackendOrOriginChanges() {
        val base = PrivateSessionNamespace(BackendKind.WORDPRESS, "tenant-a", "https://shop.example.test/")

        assertNotEquals(base.fingerprint, PrivateSessionNamespace(BackendKind.WORDPRESS, "tenant-b", "https://shop.example.test/").fingerprint)
        assertNotEquals(base.fingerprint, PrivateSessionNamespace(BackendKind.SPRING, "tenant-a", "https://shop.example.test/").fingerprint)
        assertNotEquals(base.fingerprint, PrivateSessionNamespace(BackendKind.WORDPRESS, "tenant-a", "https://other.example.test/").fingerprint)
    }

    @Test
    fun isolatesFingerprintAcrossWordPressHostModes() {
        val themeOnly = PrivateSessionNamespace(BackendKind.WORDPRESS, "tenant-a", "https://shop.example.test/", WordPressHostMode.THEME_ONLY)
        val pluginOnly = PrivateSessionNamespace(BackendKind.WORDPRESS, "tenant-a", "https://shop.example.test/", WordPressHostMode.PLUGIN_ONLY)
        val coInstall = PrivateSessionNamespace(BackendKind.WORDPRESS, "tenant-a", "https://shop.example.test/", WordPressHostMode.CO_INSTALL)

        assertNotEquals(themeOnly.fingerprint, pluginOnly.fingerprint)
        assertNotEquals(themeOnly.fingerprint, coInstall.fingerprint)
        assertNotEquals(pluginOnly.fingerprint, coInstall.fingerprint)

        assertEquals("WORDPRESS|THEME_ONLY|tenant-a|https://shop.example.test", themeOnly.fingerprint)
        assertEquals("WORDPRESS|PLUGIN_ONLY|tenant-a|https://shop.example.test", pluginOnly.fingerprint)
        assertEquals("WORDPRESS|CO_INSTALL|tenant-a|https://shop.example.test", coInstall.fingerprint)

        // Session keys are strictly isolated
        assertNotEquals(themeOnly.key("token"), pluginOnly.key("token"))
    }

    @Test
    fun rejectsHostModeOnSpringSessionNamespace() {
        assertFailsWith<IllegalArgumentException> {
            PrivateSessionNamespace(BackendKind.SPRING, "tenant-a", "https://api.example.test/", WordPressHostMode.THEME_ONLY)
        }
    }

    @Test
    fun profileExtensionPropagatesHostModeToNamespace() {
        val profile = BackendProfile(
            kind = BackendKind.WORDPRESS,
            apiRoot = "https://shop.example.test/wp-json/carmilla/v1/",
            assetRoot = "https://shop.example.test/",
            allowedAuthHosts = setOf("shop.example.test"),
            contractVersion = 1,
            manifestPath = "/wp-json/carmilla/v1/client-manifest",
            wordPressHostMode = WordPressHostMode.PLUGIN_ONLY
        )
        val ns = profile.privateSessionNamespace("tenant-x")
        assertEquals(WordPressHostMode.PLUGIN_ONLY, ns.hostMode)
        assertEquals("WORDPRESS|PLUGIN_ONLY|tenant-x|https://shop.example.test/wp-json/carmilla/v1", ns.fingerprint)
    }
}
