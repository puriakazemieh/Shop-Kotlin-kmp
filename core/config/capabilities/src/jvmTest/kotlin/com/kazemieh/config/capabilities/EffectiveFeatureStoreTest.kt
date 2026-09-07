package com.kazemieh.config.capabilities

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
}
