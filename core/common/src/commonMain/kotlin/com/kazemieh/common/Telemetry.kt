package com.kazemieh.common

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap(),
    val isPerformance: Boolean = false,
    val isError: Boolean = false
)

/** The transport receives only the approved event name and anonymous context. */
data class TelemetryDispatch(
    val name: String,
    val client: String,
    val tenantId: String
)

private val allowedTelemetryEvents = setOf(
    "app_activated",
    "pwa_installed",
    "catalog_viewed",
    "cart_changed",
    "checkout_started",
    "checkout_result",
    "app_error",
    "support_opened"
)

object Telemetry {
    private val _isOptedIn = MutableStateFlow(false)
    val isOptedIn: StateFlow<Boolean> = _isOptedIn

    private var persistConsent: (Boolean) -> Unit = {}
    private var dispatch: (suspend (TelemetryDispatch) -> Unit)? = null
    private var client = "web"
    private var tenantId = ""
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Configures the shared client after the trusted backend profile and local storage are ready. */
    fun configure(
        optedIn: Boolean,
        client: String,
        tenantId: String,
        persistConsent: (Boolean) -> Unit,
        dispatch: suspend (TelemetryDispatch) -> Unit
    ) {
        require(client in setOf("pwa", "android", "web")) { "Unsupported telemetry client." }
        require(tenantId.isNotBlank()) { "Telemetry tenant must not be blank." }
        _isOptedIn.value = optedIn
        this.client = client
        this.tenantId = tenantId
        this.persistConsent = persistConsent
        this.dispatch = dispatch
    }

    fun setOptIn(enabled: Boolean) {
        _isOptedIn.value = enabled
        persistConsent(enabled)
        AppLogger.i { "Telemetry opt-in set to: $enabled" }
    }

    fun logEvent(event: AnalyticsEvent) {
        if (!_isOptedIn.value) return
        if (event.name !in allowedTelemetryEvents) {
            AppLogger.w { "Telemetry event rejected by client allowlist: ${event.name}" }
            return
        }
        
        val redactedParams = event.params.mapValues { (_, value) ->
            if (value is String) value.redactedForLog() else value
        }
        
        val eventType = when {
            event.isError -> "ERROR_EVENT"
            event.isPerformance -> "PERF_EVENT"
            else -> "ANALYTICS_EVENT"
        }
        
        AppLogger.i { "Telemetry [$eventType]: ${event.name} | Params: $redactedParams" }
        val payload = TelemetryDispatch(event.name, client, tenantId)
        scope.launch {
            runCatching { dispatch?.invoke(payload) }
                .onFailure { error -> AppLogger.w { "Telemetry dispatch failed: ${error.message}" } }
        }
    }
}
