package com.kazemieh.config.capabilities

sealed interface FeatureCallResult<out T> {
    data class Executed<T>(val value: T) : FeatureCallResult<T>
    data object Disabled : FeatureCallResult<Nothing>
}

/**
 * guard مشترک برای use-case، repository و worker. در حالت خاموش، block اصلاً
 * اجرا نمی‌شود؛ بنابراین هیچ request شبکه یا side effect پس از آن رخ نمی‌دهد.
 */
class FeatureUseCaseGuard(private val features: ResolvedFeatures) {
    suspend fun <T> execute(featureId: String, block: suspend () -> T): FeatureCallResult<T> =
        if (features.isEnabled(featureId)) FeatureCallResult.Executed(block())
        else FeatureCallResult.Disabled

    fun isEnabled(featureId: String): Boolean = features.isEnabled(featureId)
}
