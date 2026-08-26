package com.kazemieh.config.capabilities

/**
 * شناسهٔ مرز ماژول پیکربندی قابلیت‌ها.
 *
 * مدل‌های `BackendProfile`، `FeatureManifest` و policyها در تسک‌های بعدی همین
 * فاز به این ماژول افزوده می‌شوند؛ `core:designSystem` نباید مالک آن‌ها باشد.
 */
object CapabilitiesModule {
    const val moduleId: String = "core:config:capabilities"
}
