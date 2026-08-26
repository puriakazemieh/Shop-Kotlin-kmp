package com.kazemieh.shop

import com.kazemieh.config.capabilities.BackendProfile
import com.kazemieh.config.capabilities.LocalFeatureManifestConfig
import com.kazemieh.config.capabilities.LocalFeatureManifestSource

/**
 * تنها نقطهٔ local/generated برای flagهای پایهٔ این app.
 * ابزار build در آینده می‌تواند همین artifact را تولید کند، بدون پراکنده‌سازی
 * flagها در BrandConfig یا feature moduleها.
 */
object GeneratedLocalFeatureManifest {
    fun sourceFor(profile: BackendProfile): LocalFeatureManifestSource = LocalFeatureManifestSource(
        LocalFeatureManifestConfig(
            backendKind = profile.kind,
            tenantId = "local-default",
            contentBlog = true,
            commerceCore = true,
            commercePhysical = true,
            commerceDigital = false
        )
    )
}
