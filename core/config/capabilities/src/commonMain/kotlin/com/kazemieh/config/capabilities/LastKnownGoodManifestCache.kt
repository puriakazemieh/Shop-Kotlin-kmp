package com.kazemieh.config.capabilities

/** رکورد cache فقط پس از اعتبارسنجی کامل manifest ساخته می‌شود. */
data class CachedRemoteManifest(
    val namespaceFingerprint: String,
    val manifest: FeatureManifest,
    val etag: String?,
    val expiresAtEpochMillis: Long
)

/** LKG محدود و namespaced؛ storage واقعی هر platform می‌تواند این API را جایگزین کند. */
class InMemoryLastKnownGoodManifestCache(
    private val catalog: FeatureCatalog = FeatureCatalog(),
    private val ceiling: CompiledFeatureCeiling = CompiledFeatureCeiling.shopOnly
) {
    private val entries = mutableMapOf<String, CachedRemoteManifest>()

    fun read(namespace: PrivateSessionNamespace, nowEpochMillis: Long): CachedRemoteManifest? {
        val entry = entries[namespace.fingerprint] ?: return null
        if (entry.expiresAtEpochMillis <= nowEpochMillis || !isValid(namespace, entry)) {
            entries.remove(namespace.fingerprint)
            return null
        }
        return entry
    }

    fun write(
        namespace: PrivateSessionNamespace,
        manifest: FeatureManifest,
        etag: String?,
        expiresAtEpochMillis: Long,
        nowEpochMillis: Long
    ): Boolean {
        if (expiresAtEpochMillis <= nowEpochMillis || !isValid(namespace, manifest)) return false
        entries[namespace.fingerprint] = CachedRemoteManifest(
            namespaceFingerprint = namespace.fingerprint,
            manifest = manifest,
            etag = etag,
            expiresAtEpochMillis = expiresAtEpochMillis
        )
        return true
    }

    fun invalidate(namespace: PrivateSessionNamespace) {
        entries.remove(namespace.fingerprint)
    }

    private fun isValid(namespace: PrivateSessionNamespace, entry: CachedRemoteManifest): Boolean =
        entry.namespaceFingerprint == namespace.fingerprint && isValid(namespace, entry.manifest)

    private fun isValid(namespace: PrivateSessionNamespace, manifest: FeatureManifest): Boolean = runCatching {
        require(manifest.backendKind == namespace.backendKind)
        require(manifest.tenantId == namespace.tenantId)
        ceiling.apply(catalog.resolve(manifest))
    }.isSuccess
}
