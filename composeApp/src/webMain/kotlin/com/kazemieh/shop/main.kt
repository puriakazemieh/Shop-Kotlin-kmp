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
    val params = URLSearchParams(window.location.search)
    val isLocalhost = window.location.hostname == "localhost" || window.location.hostname == "127.0.0.1"
    val api = if (isLocalhost) params.get("api")?.takeIf { it.isNotBlank() } else null
    val brandId = params.get("brand")?.takeIf { it.isNotBlank() }
    
    val sku = when {
        brandId != null -> brandId
        api != null -> "wp"
        else -> "carmila"
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
