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
    private val validWordpressProfile = BackendProfile(
        kind = BackendKind.WORDPRESS,
        apiRoot = "https://api.example.com",
        assetRoot = "https://assets.example.com",
        allowedAuthHosts = setOf("api.example.com"),
        contractVersion = 1,
        manifestPath = "/wp-json/carmilla/v1/client-manifest"
    )
    private val validSpringProfile = BackendProfile(
        kind = BackendKind.SPRING,
        apiRoot = "https://spring.example.com",
        assetRoot = "https://assets.example.com",
        allowedAuthHosts = setOf("spring.example.com"),
        contractVersion = 1,
        manifestPath = "/api/v1/manifest"
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
            backendProfile = validWordpressProfile,
            compiledFeatureIds = setOf("content.blog", "clinic.booking")
        )

        val jsonString = Json.encodeToString(spec)
        val decoded = Json.decodeFromString<ProductBuildSpec>(jsonString)

        assertEquals(spec, decoded)
    }

    @Test
    fun `same brand works with both wordpress and spring backends`() {
        val sharedBranding = BrandingConfig("carmila", "Carmila Shop", "IRR")

        val wpSpec = ProductBuildSpec(
            productKind = ProductKind.APP,
            sku = "carmila-wp",
            platform = ClientPlatform.ANDROID,
            tenant = validTenant,
            branding = sharedBranding,
            buildIdentity = validBuildIdentity,
            backendProfile = validWordpressProfile,
            compiledFeatureIds = setOf("commerce.core")
        )

        val springSpec = ProductBuildSpec(
            productKind = ProductKind.APP,
            sku = "carmila-spring",
            platform = ClientPlatform.ANDROID,
            tenant = validTenant,
            branding = sharedBranding,
            buildIdentity = validBuildIdentity,
            backendProfile = validSpringProfile,
            compiledFeatureIds = setOf("commerce.core")
        )

        assertEquals(wpSpec.branding, springSpec.branding)
        assertEquals(BackendKind.WORDPRESS, wpSpec.backendProfile.kind)
        assertEquals(BackendKind.SPRING, springSpec.backendProfile.kind)
    }

    @Test
    fun `release validation succeeds with real tenant and https`() {
        val releaseSpec = ProductBuildSpec(
            productKind = ProductKind.APP,
            sku = "com.carmilla.app.release",
            platform = ClientPlatform.ANDROID,
            tenant = TenantConfig("prod-tenant-iran-01"),
            branding = validBranding,
            buildIdentity = validBuildIdentity,
            backendProfile = validWordpressProfile,
            compiledFeatureIds = setOf("content.blog", "commerce.core")
        )

        // Must succeed without throwing
        releaseSpec.validateForRelease()
    }

    @Test
    fun `release validation fails with local-default or test tenant`() {
        val devSpec = ProductBuildSpec(
            productKind = ProductKind.APP,
            sku = "com.carmilla.app.dev",
            platform = ClientPlatform.ANDROID,
            tenant = TenantConfig("local-default"),
            branding = validBranding,
            buildIdentity = validBuildIdentity,
            backendProfile = validWordpressProfile,
            compiledFeatureIds = setOf("content.blog")
        )

        val ex = assertFailsWith<IllegalArgumentException> {
            devSpec.validateForRelease()
        }
        assertTrue(ex.message!!.contains("Real tenant is required in release build"))
    }

    @Test
    fun `release validation fails with non-https apiRoot`() {
        val httpProfile = BackendProfile(
            kind = BackendKind.WORDPRESS,
            apiRoot = "http://localhost:8080",
            assetRoot = "http://localhost:8080",
            allowedAuthHosts = setOf("localhost"),
            contractVersion = 1,
            manifestPath = "/wp-json/carmilla/v1/client-manifest"
        )
        val devSpec = ProductBuildSpec(
            productKind = ProductKind.APP,
            sku = "com.carmilla.app.dev",
            platform = ClientPlatform.ANDROID,
            tenant = TenantConfig("prod-tenant-01"),
            branding = validBranding,
            buildIdentity = validBuildIdentity,
            backendProfile = httpProfile,
            compiledFeatureIds = setOf("content.blog")
        )

        val ex = assertFailsWith<IllegalArgumentException> {
            devSpec.validateForRelease()
        }
        assertTrue(ex.message!!.contains("Release API root must use HTTPS"))
    }

    @Test
    fun `invalid blank sku throws exception`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            ProductBuildSpec(
                productKind = ProductKind.APP,
                sku = "   ", // blank sku
                platform = ClientPlatform.ANDROID,
                tenant = validTenant,
                branding = validBranding,
                buildIdentity = validBuildIdentity,
                backendProfile = validWordpressProfile,
                compiledFeatureIds = setOf()
            )
        }
        assertTrue(exception.message!!.contains("SKU must not be blank"))
    }

    @Test
    fun `sku with whitespace throws exception`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            ProductBuildSpec(
                productKind = ProductKind.APP,
                sku = "invalid sku with spaces",
                platform = ClientPlatform.ANDROID,
                tenant = validTenant,
                branding = validBranding,
                buildIdentity = validBuildIdentity,
                backendProfile = validWordpressProfile,
                compiledFeatureIds = setOf()
            )
        }
        assertTrue(exception.message!!.contains("SKU must not contain whitespace"))
    }

    @Test
    fun `unknown compiled feature throws exception`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            ProductBuildSpec(
                productKind = ProductKind.APP,
                sku = "com.carmilla.app.unknown",
                platform = ClientPlatform.ANDROID,
                tenant = validTenant,
                branding = validBranding,
                buildIdentity = validBuildIdentity,
                backendProfile = validWordpressProfile,
                compiledFeatureIds = setOf("content.blog", "unknown.nonexistent.feature")
            )
        }
        assertTrue(exception.message!!.contains("Unknown feature IDs in compiled features"))
    }

    @Test
    fun `remote manifest cannot change backend origin`() {
        // BackendProfile defines immutable local origin
        val profile = validWordpressProfile
        assertEquals("https://api.example.com", profile.apiRoot)
        assertEquals("/wp-json/carmilla/v1/client-manifest", profile.manifestPath)

        // Remote manifest client parses strictly and only accepts capability overrides;
        // it cannot override apiRoot, assetRoot, or authentication hosts.
        val transport = RemoteManifestTransport {
            // Simulated malicious remote attempting to inject rogue origin
            RemoteManifestResponse(
                statusCode = 200,
                body = """
                    {
                        "schemaVersion": 1,
                        "manifestVersion": "v1",
                        "backendProfile": "WORDPRESS",
                        "tenantId": "tenant-123",
                        "rogueOrigin": "https://malicious-site.com",
                        "features": {
                            "content.blog": true
                        }
                    }
                """.trimIndent(),
                etag = "etag-1"
            )
        }

        val client = RemoteFeatureManifestClient(
            profile = profile,
            expectedTenantId = "tenant-123",
            transport = transport,
            ceiling = CompiledFeatureCeiling(setOf("content.blog"))
        )

        // Execution of fetch should fail closed because strict Json rejects rogueOrigin unknown key
        kotlinx.coroutines.runBlocking {
            val result = client.fetch()
            assertTrue(result is RemoteManifestFetchResult.Failure)
            val failure = result as RemoteManifestFetchResult.Failure
            assertTrue(
                failure.reason.contains("rogueOrigin") ||
                    failure.reason.contains("Encountered unknown key")
            )
        }
    }
}
