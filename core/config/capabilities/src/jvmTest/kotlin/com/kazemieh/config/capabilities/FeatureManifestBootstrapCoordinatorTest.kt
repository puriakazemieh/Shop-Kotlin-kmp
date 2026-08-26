package com.kazemieh.config.capabilities

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeatureManifestBootstrapCoordinatorTest {
    private val profile = BackendProfile(BackendKind.WORDPRESS, "https://tenant.example.test/", "https://tenant.example.test/", setOf("tenant.example.test"), 1, "/manifest")
    private val namespace = profile.privateSessionNamespace("tenant-1")
    private val local = ResolvedFeatures.fromMap(mapOf("content.blog" to true, "commerce.core" to true, "commerce.physical" to true, "commerce.digital" to false))
    private val payload = """{"schemaVersion":1,"manifestVersion":"2026.08.1","backendProfile":"WORDPRESS","tenantId":"tenant-1","features":{"content.blog":true,"commerce.core":true,"commerce.physical":true,"commerce.digital":true}}"""

    @Test
    fun `remote is reduced by local source and compiled ceiling`() = runBlocking {
        val state = coordinator(RemoteManifestResponse(200, payload, "\"v1\"")).load()
        assertTrue(state is ManifestBootstrapState.Ready)
        assertEquals(ManifestBootstrapSource.REMOTE, (state as ManifestBootstrapState.Ready).source)
        assertFalse(state.features.isEnabled("commerce.digital"))
    }

    @Test
    fun `remote failure keeps safe local fallback and retry only calls remote`() = runBlocking {
        var calls = 0
        val client = client(RemoteManifestTransport { calls++; RemoteManifestResponse(503, "", null) })
        val coordinator = FeatureManifestBootstrapCoordinator(local, client, InMemoryLastKnownGoodManifestCache(), namespace, { 100L })
        val first = coordinator.load()
        coordinator.retry()
        assertTrue(first is ManifestBootstrapState.Error)
        assertEquals(2, calls)
        assertEquals(ManifestBootstrapSource.LOCAL, (first as ManifestBootstrapState.Error).source)
        assertTrue(first.features.isEnabled("commerce.core"))
    }

    @Test
    fun `valid cache is used on remote failure and stale cache is ignored`() = runBlocking {
        var now = 100L
        val cache = InMemoryLastKnownGoodManifestCache()
        cache.write(namespace, FeatureManifest(1, "cached", BackendKind.WORDPRESS, "tenant-1", mapOf("content.blog" to true)), null, 200L, now)
        val cachedState = FeatureManifestBootstrapCoordinator(local, client(RemoteManifestResponse(503, "", null)), cache, namespace, { now }, 50L).load()
        assertEquals(ManifestBootstrapSource.LAST_KNOWN_GOOD, (cachedState as ManifestBootstrapState.Error).source)
        now = 201L
        val staleState = FeatureManifestBootstrapCoordinator(local, client(RemoteManifestResponse(503, "", null)), cache, namespace, { now }, 50L).load()
        assertEquals(ManifestBootstrapSource.LOCAL, (staleState as ManifestBootstrapState.Error).source)
    }

    private fun client(response: RemoteManifestResponse): RemoteFeatureManifestClient =
        client(RemoteManifestTransport { response })

    private fun client(transport: RemoteManifestTransport) = RemoteFeatureManifestClient(profile, "tenant-1", transport)

    private fun coordinator(response: RemoteManifestResponse) = FeatureManifestBootstrapCoordinator(local, client(response), InMemoryLastKnownGoodManifestCache(), namespace, { 100L })
}
