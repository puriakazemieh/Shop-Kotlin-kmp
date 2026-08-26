package com.kazemieh.config.capabilities

enum class ManifestBootstrapSource { LOCAL, REMOTE, LAST_KNOWN_GOOD }

sealed interface ManifestBootstrapState {
    data object Loading : ManifestBootstrapState

    data class Ready(
        val features: ResolvedFeatures,
        val source: ManifestBootstrapSource,
        val etag: String?
    ) : ManifestBootstrapState

    /** خطای remote همراه با fallback امن و قابل نمایش به UI. */
    data class Error(
        val features: ResolvedFeatures,
        val source: ManifestBootstrapSource,
        val reason: String
    ) : ManifestBootstrapState
}

/** ترتیب منابع manifest را مستقل از UI و NavHost اجرا می‌کند. */
class FeatureManifestBootstrapCoordinator(
    private val localFeatures: ResolvedFeatures,
    private val remoteClient: RemoteFeatureManifestClient,
    private val cache: InMemoryLastKnownGoodManifestCache,
    private val namespace: PrivateSessionNamespace,
    private val nowEpochMillis: () -> Long,
    private val cacheTtlMillis: Long = DEFAULT_CACHE_TTL_MILLIS
) {
    init {
        require(cacheTtlMillis > 0) { "Manifest cache TTL must be positive." }
    }

    var state: ManifestBootstrapState = ManifestBootstrapState.Loading
        private set

    suspend fun load(): ManifestBootstrapState = fetchRemote()

    /** retry فقط remote را دوباره می‌خواند؛ local و cache دوباره ساخته نمی‌شوند. */
    suspend fun retry(): ManifestBootstrapState = fetchRemote()

    private suspend fun fetchRemote(): ManifestBootstrapState {
        state = ManifestBootstrapState.Loading
        val cached = cache.read(namespace, nowEpochMillis())
        state = when (val result = remoteClient.fetch(cached?.etag)) {
            is RemoteManifestFetchResult.Success -> {
                val now = nowEpochMillis()
                cache.write(namespace, result.manifest, result.etag, now + cacheTtlMillis, now)
                ManifestBootstrapState.Ready(result.features.restrictedTo(localFeatures), ManifestBootstrapSource.REMOTE, result.etag)
            }
            is RemoteManifestFetchResult.NotModified -> {
                cached?.let {
                    ManifestBootstrapState.Ready(cache.resolve(it).restrictedTo(localFeatures), ManifestBootstrapSource.LAST_KNOWN_GOOD, it.etag)
                } ?: ManifestBootstrapState.Error(localFeatures, ManifestBootstrapSource.LOCAL, "Remote manifest not modified without valid cache.")
            }
            is RemoteManifestFetchResult.Failure -> {
                cached?.let {
                    ManifestBootstrapState.Error(cache.resolve(it).restrictedTo(localFeatures), ManifestBootstrapSource.LAST_KNOWN_GOOD, result.reason)
                } ?: ManifestBootstrapState.Error(localFeatures, ManifestBootstrapSource.LOCAL, result.reason)
            }
        }
        return state
    }

    companion object {
        const val DEFAULT_CACHE_TTL_MILLIS = 86_400_000L
    }
}
