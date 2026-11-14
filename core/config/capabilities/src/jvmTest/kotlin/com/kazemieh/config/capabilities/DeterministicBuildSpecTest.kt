package com.kazemieh.config.capabilities

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeterministicBuildSpecTest {

    private val tenant = TenantConfig("prod-tenant-01")
    private val branding = BrandingConfig("chronos", "Chronos Brand", "IRR")
    private val buildId = BuildIdentity("com.kazemieh.shop", "1.0", 1)
    private val backend = BackendProfile(
        kind = BackendKind.WORDPRESS,
        apiRoot = "https://example.com/api",
        assetRoot = "https://example.com/assets",
        allowedAuthHosts = setOf("example.com"),
        contractVersion = 1,
        manifestPath = "/wp-json/carmilla/v1/client-manifest"
    )

    @Test
    fun `two identical input specifications produce identical serialized output`() {
        val specA = ProductBuildSpec(
            productKind = ProductKind.APP,
            sku = "chronos-prod",
            platform = ClientPlatform.ANDROID,
            tenant = tenant,
            branding = branding,
            buildIdentity = buildId,
            backendProfile = backend,
            compiledFeatureIds = setOf("content.blog", "commerce.core", "commerce.physical")
        )

        val specB = ProductBuildSpec(
            productKind = ProductKind.APP,
            sku = "chronos-prod",
            platform = ClientPlatform.ANDROID,
            tenant = tenant,
            branding = branding,
            buildIdentity = buildId,
            backendProfile = backend,
            compiledFeatureIds = setOf("content.blog", "commerce.core", "commerce.physical")
        )

        val jsonA = Json.encodeToString(specA)
        val jsonB = Json.encodeToString(specB)

        assertEquals(jsonA, jsonB, "Identical specs must produce byte-for-byte identical JSON serialization")
        assertEquals(specA.hashCode(), specB.hashCode(), "Identical specs must produce identical hash codes")
    }

    @Test
    fun `two SKUs produce different compiled feature ceilings and protect against remote overrides`() {
        val carmilaCeiling = CompiledFeatureCeiling(
            setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet")
        )
        val chronosCeiling = CompiledFeatureCeiling(
            setOf("content.blog", "commerce.core", "commerce.physical")
        )

        val requestedFeatures = ResolvedFeatures(
            mapOf(
                "content.blog" to true,
                "commerce.core" to true,
                "commerce.physical" to true,
                "commerce.digital" to true,
                "wallet" to true
            )
        )

        val carmilaEffective = carmilaCeiling.apply(requestedFeatures)
        val chronosEffective = chronosCeiling.apply(requestedFeatures)

        // Carmila allows wallet and digital commerce
        assertTrue(carmilaEffective.isEnabled("wallet"))
        assertTrue(carmilaEffective.isEnabled("commerce.digital"))

        // Chronos ceiling denies wallet and digital commerce even though requested
        assertFalse(chronosEffective.isEnabled("wallet"))
        assertFalse(chronosEffective.isEnabled("commerce.digital"))

        // Common features remain enabled for both
        assertTrue(carmilaEffective.isEnabled("content.blog"))
        assertTrue(chronosEffective.isEnabled("content.blog"))
    }

    @Test
    fun `zero secrets in build spec representations`() {
        val spec = ProductBuildSpec(
            productKind = ProductKind.APP,
            sku = "carmila",
            platform = ClientPlatform.ANDROID,
            tenant = tenant,
            branding = branding,
            buildIdentity = buildId,
            backendProfile = backend,
            compiledFeatureIds = setOf("content.blog", "commerce.core")
        )

        val specString = spec.toString().lowercase()
        val forbidden = listOf("password", "secret", "private_key", "token", "apikey")

        for (pattern in forbidden) {
            assertFalse(
                specString.contains("$pattern=") || specString.contains("$pattern:"),
                "Spec string must not contain secret patterns: $pattern"
            )
        }
    }
}
