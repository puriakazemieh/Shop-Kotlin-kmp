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
}
