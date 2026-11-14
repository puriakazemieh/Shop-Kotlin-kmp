package com.kazemieh.config.capabilities

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EffectiveFeatureStoreTest {
    @Test
    fun `store initialized with fallback uses fallback initially`() = runBlocking {
        val fallback = ResolvedFeatures.fromMap(mapOf("test.feature" to true))
        val store = DefaultEffectiveFeatureStore(fallback)
        assertEquals(true, store.features.value.isEnabled("test.feature"))
        assertNull(store.revision)
    }

    @Test
    fun `store rejects replay revision`() = runBlocking {
        val fallback = ResolvedFeatures.fromMap(mapOf("test.feature" to true))
        val store = DefaultEffectiveFeatureStore(fallback)
        val newFeatures = ResolvedFeatures.fromMap(mapOf("test.feature" to false, "new.feature" to true))
        store.update(ManifestBootstrapState.Ready(newFeatures, ManifestBootstrapSource.REMOTE, "v1"))
        
        assertEquals(false, store.features.value.isEnabled("test.feature"))
        assertEquals(true, store.features.value.isEnabled("new.feature"))
        assertEquals("v1", store.revision)

        val rejectedFeatures = ResolvedFeatures.fromMap(mapOf("new.feature" to false))
        store.update(ManifestBootstrapState.Ready(rejectedFeatures, ManifestBootstrapSource.REMOTE, "v1"))
        // should remain the same because revision "v1" is rejected
        assertEquals(true, store.features.value.isEnabled("new.feature"))
    }

    @Test
    fun `valid remote manifest updates reactive state flow and updates revision`() = runBlocking {
        val fallback = ResolvedFeatures.fromMap(mapOf("feature.a" to true, "feature.b" to false))
        val store = DefaultEffectiveFeatureStore(fallback)

        val remote1 = ResolvedFeatures.fromMap(mapOf("feature.a" to true, "feature.b" to true))
        store.update(ManifestBootstrapState.Ready(remote1, ManifestBootstrapSource.REMOTE, "rev-100"))

        assertTrue(store.features.first().isEnabled("feature.b"))
        assertEquals("rev-100", store.revision)

        val remote2 = ResolvedFeatures.fromMap(mapOf("feature.a" to false, "feature.b" to true))
        store.update(ManifestBootstrapState.Ready(remote2, ManifestBootstrapSource.REMOTE, "rev-101"))

        assertFalse(store.features.first().isEnabled("feature.a"))
        assertTrue(store.features.first().isEnabled("feature.b"))
        assertEquals("rev-101", store.revision)
    }

    @Test
    fun `stale or invalid error state fails closed and resets active revision`() = runBlocking {
        val fallback = ResolvedFeatures.fromMap(mapOf("feature.safe" to true, "feature.risky" to false))
        val store = DefaultEffectiveFeatureStore(fallback)

        val remote = ResolvedFeatures.fromMap(mapOf("feature.safe" to true, "feature.risky" to true))
        store.update(ManifestBootstrapState.Ready(remote, ManifestBootstrapSource.REMOTE, "rev-200"))
        assertTrue(store.features.value.isEnabled("feature.risky"))
        assertEquals("rev-200", store.revision)

        // Remote error or stale/invalid manifest triggers fail-closed fallback
        val safeFallback = ResolvedFeatures.fromMap(mapOf("feature.safe" to true, "feature.risky" to false))
        store.update(ManifestBootstrapState.Error(safeFallback, ManifestBootstrapSource.LOCAL, "Stale manifest"))

        assertFalse(store.features.value.isEnabled("feature.risky"))
        assertTrue(store.features.value.isEnabled("feature.safe"))
        assertNull(store.revision, "Active revision must be invalidated upon error/fail-closed")
    }

    @Test
    fun `store never escalates features beyond compiled ceiling`() = runBlocking {
        val ceiling = CompiledFeatureCeiling(setOf("allowed.feature"))
        val fallback = ResolvedFeatures.fromMap(mapOf("allowed.feature" to true, "forbidden.feature" to false))
        val store = DefaultEffectiveFeatureStore(fallback, ceiling)

        // Remote tries to enable a feature outside the ceiling
        val rogueRemote = ResolvedFeatures.fromMap(mapOf("allowed.feature" to true, "forbidden.feature" to true))
        store.update(ManifestBootstrapState.Ready(rogueRemote, ManifestBootstrapSource.REMOTE, "rev-300"))

        assertTrue(store.features.value.isEnabled("allowed.feature"))
        assertFalse(store.features.value.isEnabled("forbidden.feature"), "Forbidden feature must remain blocked by ceiling")
    }

    @Test
    fun `explicit revoke resets store to fail-closed fallback and invalidates revision`() = runBlocking {
        val fallback = ResolvedFeatures.fromMap(mapOf("core" to true, "premium" to false))
        val store = DefaultEffectiveFeatureStore(fallback)

        val remote = ResolvedFeatures.fromMap(mapOf("core" to true, "premium" to true))
        store.update(ManifestBootstrapState.Ready(remote, ManifestBootstrapSource.REMOTE, "rev-400"))
        assertTrue(store.features.value.isEnabled("premium"))
        assertEquals("rev-400", store.revision)

        store.revoke()

        assertFalse(store.features.value.isEnabled("premium"))
        assertTrue(store.features.value.isEnabled("core"))
        assertNull(store.revision)
    }
}
