package com.kazemieh.config.capabilities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FeatureFlagShadowModeTest {
    @Test
    fun `shadow mode preserves legacy behavior and emits redacted counts`() {
        var event: FeatureFlagShadowEvent? = null
        val legacy = ResolvedFeatures.fromMap(mapOf("content.blog" to true, "commerce.core" to true))
        val manifest = ResolvedFeatures.fromMap(mapOf("content.blog" to true, "commerce.core" to false, "commerce.physical" to true))
        val result = FeatureFlagShadowMode(FeatureFlagShadowReporter { event = it })
            .observe(legacy, manifest, "2026.08.1")

        assertEquals(legacy.asMap(), result.asMap())
        assertEquals(2, event?.changedFeatureCount)
        assertEquals(1, event?.legacyOnlyCount)
        assertEquals(1, event?.manifestOnlyCount)
        assertEquals("2026.08.1", event?.manifestVersion)
    }

    @Test
    fun `blank manifest version is rejected without telemetry`() {
        assertFailsWith<IllegalArgumentException> {
            FeatureFlagShadowMode(FeatureFlagShadowReporter { error("must not report") })
                .observe(ResolvedFeatures.fromMap(emptyMap()), ResolvedFeatures.fromMap(emptyMap()), "")
        }
    }
}
