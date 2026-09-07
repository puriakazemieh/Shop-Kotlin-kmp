package com.kazemieh.config.capabilities

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface EffectiveFeatureStore {
    val features: StateFlow<ResolvedFeatures>
    val revision: String?
    fun update(state: ManifestBootstrapState)
}

class DefaultEffectiveFeatureStore(
    initialFeatures: ResolvedFeatures
) : EffectiveFeatureStore {
    
    private val _features = MutableStateFlow(initialFeatures)
    override val features: StateFlow<ResolvedFeatures> = _features.asStateFlow()
    
    override var revision: String? = null
        private set

    override fun update(state: ManifestBootstrapState) {
        when (state) {
            is ManifestBootstrapState.Ready -> {
                if (state.etag != null && state.etag == revision) return
                _features.value = state.features
                revision = state.etag
            }
            is ManifestBootstrapState.Error -> {
                _features.value = state.features
                // We do not set revision for Error because it might not have one or we just fallback safely.
            }
            is ManifestBootstrapState.Loading -> { }
        }
    }
}
