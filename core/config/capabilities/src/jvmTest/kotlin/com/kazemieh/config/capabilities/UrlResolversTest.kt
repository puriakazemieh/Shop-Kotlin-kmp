package com.kazemieh.config.capabilities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UrlResolversTest {
    private val profile = BackendProfile(
        kind = BackendKind.WORDPRESS,
        apiRoot = "https://shop.example.test/wp-json/carmilla/v1/",
        assetRoot = "https://shop.example.test/",
        allowedAuthHosts = setOf("shop.example.test"),
        contractVersion = 1,
        manifestPath = "/wp-json/carmilla/v1/client-manifest"
    )

    @Test
    fun resolvesOnlyRelativePathsAgainstTrustedOrigins() {
        assertEquals("https://shop.example.test/wp-json/carmilla/v1/products", ProfileEndpointResolver(profile).resolve("products"))
        assertEquals("https://shop.example.test/media/hero.jpg", ProfileAssetUrlResolver(profile).resolve("/media/hero.jpg"))
    }

    @Test
    fun endpointResolverRejectsOriginReplacement() {
        assertFailsWith<IllegalArgumentException> {
            ProfileEndpointResolver(profile).resolve("https://attacker.example.test/api")
        }
    }
}
