package com.kazemieh.config.capabilities

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface EffectiveFeatureStore {
    val features: StateFlow<ResolvedFeatures>
    val revision: String?
    fun update(state: ManifestBootstrapState)
    fun revoke(fallback: ResolvedFeatures? = null)
}

class DefaultEffectiveFeatureStore(
    initialFeatures: ResolvedFeatures,
    private val ceiling: CompiledFeatureCeiling? = null
) : EffectiveFeatureStore {
    
    private val fallbackFeatures = ceiling?.apply(initialFeatures) ?: initialFeatures
    private val _features = MutableStateFlow(fallbackFeatures)
    override val features: StateFlow<ResolvedFeatures> = _features.asStateFlow()
    
    override var revision: String? = null
        private set

    override fun update(state: ManifestBootstrapState) {
        when (state) {
            is ManifestBootstrapState.Ready -> {
                // رد replay revision: اگر ریویژن تکراری باشد، تغییر نادیده گرفته می‌شود
                if (state.etag != null && state.etag == revision) return
                
                // اعمال سقف کامپایل‌شده: هیچ افزایشی بالاتر از ceiling مجاز نیست
                val effective = ceiling?.apply(state.features) ?: state.features
                _features.value = effective
                revision = state.etag
            }
            is ManifestBootstrapState.Error -> {
                // حالت fail-closed در شرایط stale یا invalid: بازگشت به fallback امن و ابطال ریویژن
                val effective = ceiling?.apply(state.features) ?: state.features
                _features.value = effective
                revision = null
            }
            is ManifestBootstrapState.Loading -> { }
        }
    }

    override fun revoke(fallback: ResolvedFeatures?) {
        // خروج کنترل‌شده و ابطال صریح ریویژن جاری (Fail-Closed)
        _features.value = fallback?.let { ceiling?.apply(it) ?: it } ?: fallbackFeatures
        revision = null
    }
}
