package com.kazemieh.config.capabilities

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class FeatureCatalogTest {
    @Test
    fun disablesChildrenWhenTheirParentIsOff() {
        val resolved = FeatureCatalog().resolve(FeatureManifest(1, "2026.08.1", BackendKind.WORDPRESS, "fixture", mapOf("academy.quiz" to true, "academy.core" to false)))
        assertFalse(resolved.isEnabled("academy.quiz"))
    }

    @Test
    fun rejectsUnknownFeatureSchemaAndCycles() {
        assertFailsWith<IllegalArgumentException> { FeatureCatalog().resolve(FeatureManifest(1, "2026.08.1", BackendKind.WORDPRESS, "fixture", mapOf("unknown" to true))) }
        assertFailsWith<IllegalArgumentException> { FeatureCatalog().resolve(FeatureManifest(2, "2026.08.1", BackendKind.WORDPRESS, "fixture", emptyMap())) }
        assertFailsWith<IllegalArgumentException> { FeatureCatalog(listOf(FeatureDefinition("a", setOf("b")), FeatureDefinition("b", setOf("a")))) }
    }
}
