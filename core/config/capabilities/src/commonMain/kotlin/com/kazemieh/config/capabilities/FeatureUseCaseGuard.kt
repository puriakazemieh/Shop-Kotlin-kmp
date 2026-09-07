package com.kazemieh.config.capabilities

sealed interface FeatureCallResult<out T> {
    data class Executed<T>(val value: T) : FeatureCallResult<T>
    data object Disabled : FeatureCallResult<Nothing>
}

/**
 * guard مشترک برای use-case، repository و worker. در حالت خاموش، block اصلاً
 * اجرا نمی‌شود؛ بنابراین هیچ request شبکه یا side effect پس از آن رخ نمی‌دهد.
 */
class FeatureUseCaseGuard(private val store: EffectiveFeatureStore) {
    @Deprecated("Use EffectiveFeatureStore", ReplaceWith("FeatureUseCaseGuard(DefaultEffectiveFeatureStore(features))"))
    constructor(features: ResolvedFeatures) : this(DefaultEffectiveFeatureStore(features))

    suspend fun <T> execute(featureId: String, block: suspend () -> T): FeatureCallResult<T> =
        if (store.features.value.isEnabled(featureId)) FeatureCallResult.Executed(block())
        else FeatureCallResult.Disabled

    fun isEnabled(featureId: String): Boolean = store.features.value.isEnabled(featureId)
}
