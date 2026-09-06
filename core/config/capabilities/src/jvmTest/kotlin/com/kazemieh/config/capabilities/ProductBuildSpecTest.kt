package com.kazemieh.config.capabilities

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith

class ProductBuildSpecTest {

    private val validTenant = TenantConfig("tenant-123")
    private val validBranding = BrandingConfig("brand-1", "My Brand", "USD")
    private val validBuildIdentity = BuildIdentity("com.example.app", "1.0.0", 1)
    private val validBackendProfile = BackendProfile(
        kind = BackendKind.WORDPRESS,
        apiRoot = "https://api.example.com",
        assetRoot = "https://assets.example.com",
        allowedAuthHosts = setOf("api.example.com"),
        contractVersion = 1,
        manifestPath = "/wp-json/carmilla/v1/client-manifest"
    )

    @Test
    fun `serialization roundtrip succeeds`() {
        val spec = ProductBuildSpec(
            productKind = ProductKind.APP,
            sku = "com.carmilla.app.clinic",
            platform = ClientPlatform.ANDROID,
            tenant = validTenant,
            branding = validBranding,
            buildIdentity = validBuildIdentity,
            backendProfile = validBackendProfile,
            compiledFeatureIds = setOf("content.blog", "clinic.booking")
        )

        val jsonString = Json.encodeToString(spec)
        val decoded = Json.decodeFromString<ProductBuildSpec>(jsonString)

        assertEquals(spec, decoded)
    }

    @Test
    fun `invalid sku throws exception`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            ProductBuildSpec(
                productKind = ProductKind.APP,
                sku = "   ", // blank sku
                platform = ClientPlatform.ANDROID,
                tenant = validTenant,
                branding = validBranding,
                buildIdentity = validBuildIdentity,
                backendProfile = validBackendProfile,
                compiledFeatureIds = setOf()
            )
        }
        assertTrue(exception.message!!.contains("SKU must not be blank"))
    }
}
