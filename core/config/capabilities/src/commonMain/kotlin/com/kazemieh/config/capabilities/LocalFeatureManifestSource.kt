package com.kazemieh.config.capabilities

/**
 * ورودی generated/local هر app پیش از دریافت هر manifest راه‌دور.
 *
 * فقط چهار feature پایه در این مرحله قابل تنظیم‌اند تا build config یا برند به
 * منبع پراکندهٔ flagها تبدیل نشود. اعتبارسنجی ناموفق همواره به مجموعهٔ خالی
 * برمی‌گردد؛ هیچ config محلی نامعتبر نباید feature را روشن کند.
 */
data class LocalFeatureManifestConfig(
    val schemaVersion: Int = 1,
    val manifestVersion: String = "local-v1",
    val backendKind: BackendKind,
    val tenantId: String,
    val contentBlog: Boolean,
    val commerceCore: Boolean,
    val commercePhysical: Boolean,
    val commerceDigital: Boolean
) {
    fun toManifest(): FeatureManifest = FeatureManifest(
        schemaVersion = schemaVersion,
        manifestVersion = manifestVersion,
        backendKind = backendKind,
        tenantId = tenantId,
        features = mapOf(
            "content.blog" to contentBlog,
            "commerce.core" to commerceCore,
            "commerce.physical" to commercePhysical,
            "commerce.digital" to commerceDigital
        )
    )
}

class LocalFeatureManifestSource(
    private val config: LocalFeatureManifestConfig,
    private val catalog: FeatureCatalog = FeatureCatalog(),
    private val ceiling: CompiledFeatureCeiling = CompiledFeatureCeiling.shopOnly
) {
    fun resolveFor(expectedBackend: BackendKind): ResolvedFeatures = runCatching {
        require(config.backendKind == expectedBackend) { "Local manifest backend mismatch." }
        ceiling.apply(catalog.resolve(config.toManifest()))
    }.getOrElse { ResolvedFeatures(emptyMap()) }
}
