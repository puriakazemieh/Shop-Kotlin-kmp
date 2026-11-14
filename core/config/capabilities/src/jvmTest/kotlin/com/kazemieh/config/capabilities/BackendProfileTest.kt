package com.kazemieh.config.capabilities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.json.Json

class BackendProfileTest {
    @Test
    fun keepsTrustedOriginsOutsideTheManifest() {
        val profile = BackendProfile(
            kind = BackendKind.WORDPRESS,
            apiRoot = "https://shop.example.test/wp-json/carmilla/v1/",
            assetRoot = "https://shop.example.test/",
            allowedAuthHosts = setOf("shop.example.test"),
            contractVersion = 1,
            manifestPath = "/wp-json/carmilla/v1/client-manifest"
        )

        assertEquals(setOf("shop.example.test"), profile.allowedAuthHosts)
        assertFailsWith<Exception> {
            Json.decodeFromString<FeatureManifest>("""{"schemaVersion":1,"manifestVersion":"2026.08.1","backendKind":"WORDPRESS","tenantId":"fixture","features":{},"apiRoot":"https://attacker.test/"}""")
        }
    }

    @Test
    fun rejectsUntrustedProfileValues() {
        assertFailsWith<IllegalArgumentException> {
            BackendProfile(BackendKind.SPRING, "http://api.example.test/", "https://assets.example.test/", setOf("api.example.test"), 1, "/manifest")
        }
        assertFailsWith<IllegalArgumentException> {
            BackendProfile(BackendKind.SPRING, "https://api.example.test/", "https://assets.example.test/", emptySet(), 1, "manifest")
        }
    }

    @Test
    fun rejectsSpringProfileWithWordPressHostMode() {
        assertFailsWith<IllegalArgumentException> {
            BackendProfile(
                kind = BackendKind.SPRING,
                apiRoot = "https://api.example.test/",
                assetRoot = "https://assets.example.test/",
                allowedAuthHosts = setOf("api.example.test"),
                contractVersion = 1,
                manifestPath = "/api/v1/manifest",
                wordPressHostMode = WordPressHostMode.THEME_ONLY
            )
        }
        assertFailsWith<IllegalArgumentException> {
            BackendProfile(
                kind = BackendKind.SPRING,
                apiRoot = "https://api.example.test/",
                assetRoot = "https://assets.example.test/",
                allowedAuthHosts = setOf("api.example.test"),
                contractVersion = 1,
                manifestPath = "/api/v1/manifest",
                wordPressHostMode = WordPressHostMode.CO_INSTALL
            )
        }
    }

    @Test
    fun allowsWordPressProfileWithAnyValidHostModeAndCalculatesEffectiveHostMode() {
        val modes = listOf(WordPressHostMode.THEME_ONLY, WordPressHostMode.PLUGIN_ONLY, WordPressHostMode.CO_INSTALL)
        for (mode in modes) {
            val profile = BackendProfile(
                kind = BackendKind.WORDPRESS,
                apiRoot = "https://shop.example.test/wp-json/carmilla/v1/",
                assetRoot = "https://shop.example.test/",
                allowedAuthHosts = setOf("shop.example.test"),
                contractVersion = 1,
                manifestPath = "/wp-json/carmilla/v1/client-manifest",
                wordPressHostMode = mode
            )
            assertEquals(mode, profile.wordPressHostMode)
            assertEquals(mode, profile.effectiveHostMode)

            // Serialization roundtrip
            val encoded = Json.encodeToString(BackendProfile.serializer(), profile)
            val decoded = Json.decodeFromString(BackendProfile.serializer(), encoded)
            assertEquals(profile, decoded)
        }

        // Default WordPress host mode is CO_INSTALL when null
        val defaultProfile = BackendProfile(
            kind = BackendKind.WORDPRESS,
            apiRoot = "https://shop.example.test/wp-json/carmilla/v1/",
            assetRoot = "https://shop.example.test/",
            allowedAuthHosts = setOf("shop.example.test"),
            contractVersion = 1,
            manifestPath = "/wp-json/carmilla/v1/client-manifest",
            wordPressHostMode = null
        )
        assertEquals(WordPressHostMode.CO_INSTALL, defaultProfile.effectiveHostMode)

        // Spring effective host mode is always null
        val springProfile = BackendProfile(
            kind = BackendKind.SPRING,
            apiRoot = "https://api.example.test/",
            assetRoot = "https://assets.example.test/",
            allowedAuthHosts = setOf("api.example.test"),
            contractVersion = 1,
            manifestPath = "/api/v1/manifest",
            wordPressHostMode = null
        )
        assertEquals(null, springProfile.effectiveHostMode)
    }
}
