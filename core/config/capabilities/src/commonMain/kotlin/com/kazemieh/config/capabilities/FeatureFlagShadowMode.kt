package com.kazemieh.config.capabilities

/** telemetry حداقلی و redacted؛ هیچ tenant، origin یا مقدار خام flag ثبت نمی‌شود. */
data class FeatureFlagShadowEvent(
    val manifestVersion: String,
    val changedFeatureCount: Int,
    val legacyOnlyCount: Int,
    val manifestOnlyCount: Int
)

fun interface FeatureFlagShadowReporter {
    fun report(event: FeatureFlagShadowEvent)
}

/** مقایسه فقط مشاهده‌ای است و رفتار runtime را به manifest جدید واگذار نمی‌کند. */
class FeatureFlagShadowMode(private val reporter: FeatureFlagShadowReporter) {
    fun observe(
        legacy: ResolvedFeatures,
        manifest: ResolvedFeatures,
        manifestVersion: String
    ): ResolvedFeatures {
        require(manifestVersion.isNotBlank()) { "Manifest version must not be blank." }
        val legacyMap = legacy.asMap()
        val manifestMap = manifest.asMap()
        val ids = legacyMap.keys + manifestMap.keys
        var legacyOnly = 0
        var manifestOnly = 0
        ids.forEach { id ->
            val oldValue = legacyMap[id] == true
            val newValue = manifestMap[id] == true
            if (oldValue && !newValue) legacyOnly++
            if (!oldValue && newValue) manifestOnly++
        }
        reporter.report(
            FeatureFlagShadowEvent(
                manifestVersion = manifestVersion,
                changedFeatureCount = legacyOnly + manifestOnly,
                legacyOnlyCount = legacyOnly,
                manifestOnlyCount = manifestOnly
            )
        )
        return legacy
    }
}
