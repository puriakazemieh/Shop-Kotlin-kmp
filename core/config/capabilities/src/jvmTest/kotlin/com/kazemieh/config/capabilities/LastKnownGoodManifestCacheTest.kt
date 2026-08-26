package com.kazemieh.config.capabilities

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LastKnownGoodManifestCacheTest {
    private val wordpress = PrivateSessionNamespace(BackendKind.WORDPRESS, "tenant-1", "https://wp.example.test/")
    private val spring = PrivateSessionNamespace(BackendKind.SPRING, "tenant-1", "https://spring.example.test/")
    private val otherTenant = PrivateSessionNamespace(BackendKind.WORDPRESS, "tenant-2", "https://wp.example.test/")
    private val manifest = FeatureManifest(1, "2026.08.1", BackendKind.WORDPRESS, "tenant-1", mapOf("content.blog" to true, "commerce.core" to true))

    @Test
    fun `writes only validated non expired entries and expires them`() {
        val cache = InMemoryLastKnownGoodManifestCache()
        assertFalse(cache.write(wordpress, manifest, "\"v1\"", 100L, 100L))
        assertTrue(cache.write(wordpress, manifest, "\"v1\"", 200L, 100L))
        assertNotNull(cache.read(wordpress, 199L))
        assertNull(cache.read(wordpress, 200L))
    }

    @Test
    fun `backend tenant and invalidation are isolated`() {
        val cache = InMemoryLastKnownGoodManifestCache()
        assertTrue(cache.write(wordpress, manifest, null, 500L, 100L))
        assertNull(cache.read(spring, 101L))
        assertNull(cache.read(otherTenant, 101L))
        assertNotNull(cache.read(wordpress, 101L))
        cache.invalidate(wordpress)
        assertNull(cache.read(wordpress, 101L))
    }

    @Test
    fun `invalid manifest is never reactivated`() {
        val cache = InMemoryLastKnownGoodManifestCache()
        val invalid = manifest.copy(features = mapOf("unknown.feature" to true))
        assertFalse(cache.write(wordpress, invalid, null, 500L, 100L))
        assertNull(cache.read(wordpress, 101L))
    }
}
