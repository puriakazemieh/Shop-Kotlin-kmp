package com.kazemieh.config.capabilities

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RemoteFeatureManifestClientTest {
    private val profile = BackendProfile(
        kind = BackendKind.WORDPRESS,
        apiRoot = "https://tenant.example.test/",
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
            timeoutMillis = 1_250L
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
            transport = RemoteManifestTransport { RemoteManifestResponse(304, "", null) }
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
                transport = RemoteManifestTransport { RemoteManifestResponse(200, body, null) }
            ).fetch()
            assertTrue(result is RemoteManifestFetchResult.Failure)
        }

        val timeout = RemoteFeatureManifestClient(
            profile = profile,
            expectedTenantId = "tenant-1",
            transport = RemoteManifestTransport { error("timeout") }
        ).fetch()
        assertTrue(timeout is RemoteManifestFetchResult.Failure)
    }

    private fun validPayload() =
        """{"schemaVersion":1,"manifestVersion":"2026.08.1","backendProfile":"WORDPRESS","tenantId":"tenant-1","minimumAppVersion":"1.0.0","issuedAt":"2026-08-26T00:00:00Z","expiresAt":"2026-08-27T00:00:00Z","features":{"content.blog":true,"commerce.core":true,"commerce.physical":true,"commerce.digital":false}}"""
}
