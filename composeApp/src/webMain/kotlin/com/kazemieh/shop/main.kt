package com.kazemieh.shop

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.kazemieh.common.PaymentEventBus
import com.kazemieh.common.PaymentResult
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val (sku, apiOverride) = resolveSkuAndApi()
    initKoin(sku = sku, apiBaseUrlOverride = apiOverride)
    handleWebDeepLink()
    ComposeViewport {
        App()
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
