package com.kazemieh.common

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap(),
    val isPerformance: Boolean = false,
    val isError: Boolean = false
)

object Telemetry {
    private val _isOptedIn = MutableStateFlow(false)
    val isOptedIn: StateFlow<Boolean> = _isOptedIn

    fun setOptIn(enabled: Boolean) {
        _isOptedIn.value = enabled
        AppLogger.i { "Telemetry opt-in set to: $enabled" }
    }

    fun logEvent(event: AnalyticsEvent) {
        if (!_isOptedIn.value) return
        
        val redactedParams = event.params.mapValues { (_, value) ->
            if (value is String) value.redactedForLog() else value
        }
        
        val eventType = when {
            event.isError -> "ERROR_EVENT"
            event.isPerformance -> "PERF_EVENT"
            else -> "ANALYTICS_EVENT"
        }
        
        // In a real staging/prod setup, this would dispatch to a backend or Firebase Analytics.
        AppLogger.i { "Telemetry [$eventType]: ${event.name} | Params: $redactedParams" }
    }
}
