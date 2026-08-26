package com.kazemieh.config.capabilities

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompiledFeaturePolicyTest {
    @Test
    fun cannotEnableSensitiveFeaturesOutsideTheCompiledCeiling() {
        val resolved = FeatureCatalog().resolve(FeatureManifest(1, "2026.08.1", BackendKind.WORDPRESS, "fixture", mapOf(
            "content.blog" to true, "commerce.core" to true, "clinic.booking" to true,
            "psych.tests" to true, "admin.mobile" to true
        )))

        val effective = CompiledFeatureCeiling.shopOnly.apply(resolved)

        assertTrue(effective.isEnabled("commerce.core"))
        assertFalse(effective.isEnabled("clinic.booking"))
        assertFalse(effective.isEnabled("psych.tests"))
        assertFalse(effective.isEnabled("admin.mobile"))
    }
}
