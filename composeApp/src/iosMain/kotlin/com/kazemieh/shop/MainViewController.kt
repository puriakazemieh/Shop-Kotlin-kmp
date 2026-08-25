package com.kazemieh.shop

import androidx.compose.ui.window.ComposeUIViewController
import com.kazemieh.common.PaymentEventBus
import com.kazemieh.common.PaymentResult
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import platform.UIKit.UIViewController

private var koinInitialized = false

fun MainViewController(): UIViewController {
    if (!koinInitialized) {
        initKoin()
        koinInitialized = true
    }

    return ComposeUIViewController {
        App()
    }
}

fun handleDeepLink(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    if (nsUrl.scheme == "myapp" && nsUrl.host == "payment-result") {
        val components = NSURLComponents.componentsWithURL(nsUrl, false)
        val queryItems = components?.queryItems as? List<NSURLQueryItem>
        val token = queryItems?.find { it.name == "token" }?.value
        if (token != null) {
            PaymentEventBus.publish(PaymentResult(token))
        }
    }
}
