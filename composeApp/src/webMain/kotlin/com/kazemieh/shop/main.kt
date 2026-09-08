package com.kazemieh.shop

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.kazemieh.common.PaymentEventBus
import com.kazemieh.common.PaymentResult
import com.kazemieh.common.TokenExpiredEventBus
import com.kazemieh.common.AuthState
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val (sku, apiOverride) = resolveSkuAndApi()
    initKoin(sku = sku, apiBaseUrlOverride = apiOverride)
    handleWebDeepLink()
    handleWebLogoutPurge()
    ComposeViewport {
        App()
    }
}

private fun handleWebLogoutPurge() {
    GlobalScope.launch {
        TokenExpiredEventBus.events.collectLatest { state ->
            if (state == AuthState.Unauthenticated) {
                try {
                    val caches = window.asDynamic().caches
                    if (caches != null) {
                        caches.keys().then { keys: Array<String> ->
                            keys.forEach { key ->
                                caches.delete(key)
                            }
                        }
                    }
                } catch (e: Exception) {
                    console.log("Failed to clear SW caches on logout: " + e.message)
                }
            }
        }
    }
}

private fun resolveSkuAndApi(): Pair<String, String?> {
    // In production, app-config.json sets window.appConfig
    val appConfig = window.asDynamic().appConfig
    
    val sku = if (appConfig != null) {
        appConfig.sku as String
    } else {
        "carmila"
    }

    val api = if (appConfig != null) {
        appConfig.backend.apiRoot as String
    } else {
        null
    }
    
    return Pair(sku, api)
}

private fun handleWebDeepLink() {
    val searchParams = URLSearchParams(window.location.search)
    val token = searchParams.get("token")
    
    if (token != null) {
        PaymentEventBus.publish(PaymentResult(token))
    }
}
