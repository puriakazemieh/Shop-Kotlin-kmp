package com.kazemieh.config.capabilities

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RemoteFeatureManifestClientTest {
    private val profile = BackendProfile(
        kind = BackendKind.WORDPRESS,
        apiRoot = "https://tenant.example.test/wp-json/carmilla/v1/",
        assetRoot = "https://tenant.example.test/",
        allowedAuthHosts = setOf("tenant.example.test"),
        contractVersion = 1,
        manifestPath = "/wp-json/carmilla/v1/client-manifest"
    )

    @Test
    fun `sends trusted url timeout and etag and resolves manifest`() = runBlocking {
        var request: RemoteManifestRequest? = null
        val transport = RemoteManifestTransport {
            request = it
            RemoteManifestResponse(200, validPayload(), "\"v1\"")
        }
        val result = RemoteFeatureManifestClient(
            profile = profile,
            expectedTenantId = "tenant-1",
            transport = transport,
            timeoutMillis = 1_250L,
            ceiling = CompiledFeatureCeiling(setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"))
        ).fetch("\"old\"")

        assertEquals("https://tenant.example.test/wp-json/carmilla/v1/client-manifest", request?.url)
        assertEquals(1_250L, request?.timeoutMillis)
        assertEquals("\"old\"", request?.ifNoneMatch)
        assertTrue(result is RemoteManifestFetchResult.Success)
        assertTrue((result as RemoteManifestFetchResult.Success).features.isEnabled("commerce.core"))
    }

    @Test
    fun `not modified preserves etag`() = runBlocking {
        val result = RemoteFeatureManifestClient(
            profile = profile,
            expectedTenantId = "tenant-1",
            transport = RemoteManifestTransport { RemoteManifestResponse(304, "", null) },
            ceiling = CompiledFeatureCeiling(setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"))
        ).fetch("\"cached\"")

        assertEquals(RemoteManifestFetchResult.NotModified("\"cached\""), result)
    }

    @Test
    fun `invalid schema backend tenant unknown feature and timeout fail closed`() = runBlocking {
        val invalidPayloads = listOf(
            validPayload().replace("\"schemaVersion\":1", "\"schemaVersion\":2"),
            validPayload().replace("\"backendProfile\":\"WORDPRESS\"", "\"backendProfile\":\"SPRING\""),
            validPayload().replace("\"tenantId\":\"tenant-1\"", "\"tenantId\":\"other\""),
            validPayload().replace("\"commerce.core\":true", "\"unknown.feature\":true")
        )
        invalidPayloads.forEach { body ->
            val result = RemoteFeatureManifestClient(
                profile = profile,
                expectedTenantId = "tenant-1",
                transport = RemoteManifestTransport { RemoteManifestResponse(200, body, null) },
                ceiling = CompiledFeatureCeiling(setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"))
            ).fetch()
            assertTrue(result is RemoteManifestFetchResult.Failure)
        }

        val timeout = RemoteFeatureManifestClient(
            profile = profile,
            expectedTenantId = "tenant-1",
            transport = RemoteManifestTransport { error("timeout") },
            ceiling = CompiledFeatureCeiling(setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"))
        ).fetch()
        assertTrue(timeout is RemoteManifestFetchResult.Failure)
    }

    @Test
    fun `hostMode matching succeeds and mismatching or invalid hostMode fails closed`() = runBlocking {
        val wpProfileWithMode = profile.copy(wordPressHostMode = WordPressHostMode.PLUGIN_ONLY)
        val validPayloadWithMode = validPayload().replace(
            "\"tenantId\":\"tenant-1\",",
            "\"tenantId\":\"tenant-1\",\"hostMode\":\"PLUGIN_ONLY\","
        )

        // Matching hostMode succeeds
        val successResult = RemoteFeatureManifestClient(
            profile = wpProfileWithMode,
            expectedTenantId = "tenant-1",
            transport = RemoteManifestTransport { RemoteManifestResponse(200, validPayloadWithMode, null) },
            ceiling = CompiledFeatureCeiling(setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"))
        ).fetch()
        assertTrue(successResult is RemoteManifestFetchResult.Success)

        // Mismatched hostMode fails closed
        val mismatchedPayload = validPayloadWithMode.replace("\"PLUGIN_ONLY\"", "\"THEME_ONLY\"")
        val mismatchedResult = RemoteFeatureManifestClient(
            profile = wpProfileWithMode,
            expectedTenantId = "tenant-1",
            transport = RemoteManifestTransport { RemoteManifestResponse(200, mismatchedPayload, null) },
            ceiling = CompiledFeatureCeiling(setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"))
        ).fetch()
        assertTrue(mismatchedResult is RemoteManifestFetchResult.Failure)

        // Spring profile receiving hostMode fails closed
        val springProfile = BackendProfile(
            kind = BackendKind.SPRING,
            apiRoot = "https://api.example.test/",
            assetRoot = "https://assets.example.test/",
            allowedAuthHosts = setOf("api.example.test"),
            contractVersion = 1,
            manifestPath = "/api/v1/manifest"
        )
        val springPayloadWithHostMode = validPayloadWithMode.replace("\"WORDPRESS\"", "\"SPRING\"")
        val springResult = RemoteFeatureManifestClient(
            profile = springProfile,
            expectedTenantId = "tenant-1",
            transport = RemoteManifestTransport { RemoteManifestResponse(200, springPayloadWithHostMode, null) },
            ceiling = CompiledFeatureCeiling(setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"))
        ).fetch()
        assertTrue(springResult is RemoteManifestFetchResult.Failure)
    }

    private fun validPayload() =
        """{"schemaVersion":1,"manifestVersion":"2026.08.1","backendProfile":"WORDPRESS","tenantId":"tenant-1","minimumAppVersion":"1.0.0","issuedAt":"2026-08-26T00:00:00Z","expiresAt":"2026-08-27T00:00:00Z","features":{"content.blog":true,"commerce.core":true,"commerce.physical":true,"commerce.digital":false}}"""
}
