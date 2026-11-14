package com.kazemieh.shop

import com.kazemieh.config.capabilities.BackendKind
import com.kazemieh.config.capabilities.CompiledFeatureCeiling
import com.kazemieh.config.capabilities.FeatureCatalog
import com.kazemieh.config.capabilities.FeatureManifest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GeneratedProductSpecsTest {

    @Test
    fun `all configured SKUs are generated and available in specs map`() {
        val expectedSkus = listOf("academy", "atris", "carmila", "chronos", "psych", "wp")
        val actualSkus = GeneratedProductSpecs.specs.keys.toList()

        // Deterministic sorting check: keys are sorted alphabetically
        assertEquals(expectedSkus.sorted(), actualSkus)
        for (sku in expectedSkus) {
            val spec = GeneratedProductSpecs.specs[sku]
            assertNotNull(spec, "Spec for $sku should exist")
            assertEquals(sku, spec.sku)
            assertTrue(spec.sku.isNotBlank() && !spec.sku.contains(' '))
        }
    }

    @Test
    fun `two SKUs enforce different compiled feature ceilings`() {
        val carmilaSpec = GeneratedProductSpecs.specs["carmila"]
        val chronosSpec = GeneratedProductSpecs.specs["chronos"]
        val academySpec = GeneratedProductSpecs.specs["academy"]

        assertNotNull(carmilaSpec)
        assertNotNull(chronosSpec)
        assertNotNull(academySpec)

        // Carmila includes wallet and digital commerce
        assertTrue("wallet" in carmilaSpec.compiledFeatureIds)
        assertTrue("commerce.digital" in carmilaSpec.compiledFeatureIds)

        // Chronos excludes wallet and digital commerce
        assertFalse("wallet" in chronosSpec.compiledFeatureIds)
        assertFalse("commerce.digital" in chronosSpec.compiledFeatureIds)

        // Academy includes academy.core and commerce.digital but excludes commerce.physical
        assertTrue("academy.core" in academySpec.compiledFeatureIds)
        assertTrue("commerce.digital" in academySpec.compiledFeatureIds)
        assertFalse("commerce.physical" in academySpec.compiledFeatureIds)
    }

    @Test
    fun `build lacking a compiled feature cannot be remotely turned on`() {
        val chronosSpec = GeneratedProductSpecs.specs["chronos"]!!
        val ceiling = CompiledFeatureCeiling(chronosSpec.compiledFeatureIds)
        val catalog = FeatureCatalog()

        // Remote manifest attempts to aggressively enable features outside chronos ceiling
        val remoteManifest = FeatureManifest(
            schemaVersion = 1,
            manifestVersion = "v2026-remote",
            backendKind = BackendKind.WORDPRESS,
            tenantId = "local-default",
            features = mapOf(
                "content.blog" to true,
                "commerce.core" to true,
                "commerce.physical" to true,
                "commerce.digital" to true,
                "wallet" to true,
                "academy.core" to true,
                "psych.tests" to true,
                "clinic.booking" to true
            )
        )

        val resolvedByCatalog = catalog.resolve(remoteManifest)
        val effectiveFeatures = ceiling.apply(resolvedByCatalog)

        // Allowed features in chronos ceiling remain enabled
        assertTrue(effectiveFeatures.isEnabled("content.blog"))
        assertTrue(effectiveFeatures.isEnabled("commerce.core"))
        assertTrue(effectiveFeatures.isEnabled("commerce.physical"))

        // Uncompiled features MUST be blocked and disabled by the ceiling
        assertFalse(effectiveFeatures.isEnabled("commerce.digital"), "commerce.digital must remain disabled")
        assertFalse(effectiveFeatures.isEnabled("wallet"), "wallet must remain disabled")
        assertFalse(effectiveFeatures.isEnabled("academy.core"), "academy.core must remain disabled")
        assertFalse(effectiveFeatures.isEnabled("psych.tests"), "psych.tests must remain disabled")
        assertFalse(effectiveFeatures.isEnabled("clinic.booking"), "clinic.booking must remain disabled")
    }

    @Test
    fun `secret hygiene - zero secrets in generated specs output`() {
        val forbiddenSubstrings = listOf("password", "secret", "private_key", "token", "apikey")

        for ((sku, spec) in GeneratedProductSpecs.specs) {
            val serialized = spec.toString().lowercase()
            for (forbidden in forbiddenSubstrings) {
                assertFalse(
                    serialized.contains("$forbidden=") || serialized.contains("$forbidden:"),
                    "Spec for $sku contains potential secret pattern: $forbidden"
                )
            }
        }
    }

    @Test
    fun `localConfigs match generated specs backend and tenant`() {
        for ((sku, spec) in GeneratedProductSpecs.specs) {
            val localConfig = GeneratedProductSpecs.localConfigs[sku]
            assertNotNull(localConfig, "Local config for $sku must exist")
            assertEquals(spec.backendProfile.kind, localConfig.backendKind)
            assertEquals(spec.tenant.id, localConfig.tenantId)
        }
    }
}
