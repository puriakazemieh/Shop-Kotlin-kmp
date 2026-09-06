package com.kazemieh.config.capabilities

/** سقف قابلیت‌های کامپایل‌شده؛ Manifest فقط اجازهٔ کاهش این مجموعه را دارد. */
data class CompiledFeatureCeiling(val allowedFeatureIds: Set<String>) {
    fun apply(resolved: ResolvedFeatures): ResolvedFeatures =
        ResolvedFeatures(resolved.asMap().mapValues { (id, enabled) -> enabled && id in allowedFeatureIds })
}
