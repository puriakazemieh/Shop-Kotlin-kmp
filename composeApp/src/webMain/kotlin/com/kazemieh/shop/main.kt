package com.kazemieh.shop

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.kazemieh.common.PaymentEventBus
import com.kazemieh.common.PaymentResult
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    handleWebDeepLink()
    ComposeViewport {
        App()
    }
}

private fun handleWebDeepLink() {
    val searchParams = URLSearchParams(window.location.search)
    val status = searchParams.get("status")
    val orderId = searchParams.get("orderId")
    
    if (status != null) {
        PaymentEventBus.publish(PaymentResult(status, orderId))
    }
}
