package com.kazemieh.config.capabilities

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalFeatureManifestSourceTest {
    @Test
    fun resolvesFourPrimaryFlagsFromOneLocalConfig() {
        val resolved = LocalFeatureManifestSource(
            LocalFeatureManifestConfig(
                backendKind = BackendKind.WORDPRESS,
                tenantId = "local-shop",
                contentBlog = true,
                commerceCore = true,
                commercePhysical = true,
                commerceDigital = false
            )
        ).resolveFor(BackendKind.WORDPRESS)

        assertTrue(resolved.isEnabled("content.blog"))
        assertTrue(resolved.isEnabled("commerce.core"))
        assertTrue(resolved.isEnabled("commerce.physical"))
        assertFalse(resolved.isEnabled("commerce.digital"))
    }

    @Test
    fun failsClosedForInvalidOrWrongBackendLocalConfig() {
        val invalid = LocalFeatureManifestSource(
            LocalFeatureManifestConfig(
                schemaVersion = 2,
                backendKind = BackendKind.WORDPRESS,
                tenantId = "local-shop",
                contentBlog = true,
                commerceCore = true,
                commercePhysical = true,
                commerceDigital = true
            )
        )

        assertFalse(invalid.resolveFor(BackendKind.WORDPRESS).isEnabled("commerce.core"))
        assertFalse(invalid.resolveFor(BackendKind.SPRING).isEnabled("content.blog"))
    }
}
