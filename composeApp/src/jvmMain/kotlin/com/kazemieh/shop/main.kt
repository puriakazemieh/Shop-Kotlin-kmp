package com.kazemieh.shop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.kazemieh.common.PaymentEventBus
import com.kazemieh.common.PaymentResult
import java.awt.Desktop
import java.awt.Dimension
import java.net.URI

fun main(args: Array<String>) = application {
    // انتخابِ برند برای دسکتاپ با آرگومانِ اجرا: -Dbrand=atris (پیش‌فرض carmila)
    initKoin(brand = com.kazemieh.designsystem.brand.BrandRegistry.byId(System.getProperty("brand")))

    // Handle deep link from arguments (if app was launched with URI)
    args.firstOrNull()?.let { handleUri(it) }

    // Handle deep link while app is running
    try {
        if (Desktop.isDesktopSupported()) {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.APP_OPEN_URI)) {
                desktop.setOpenURIHandler { event ->
                    handleUri(event.uri.toString())
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            width = 1000.dp,
            height = 700.dp
        )
    ) {
        window.minimumSize = Dimension(800, 600)
        App()
    }
}

private fun handleUri(uriString: String) {
    try {
        val uri = URI(uriString)
        if (uri.scheme == "myapp" && uri.host == "payment-result") {
            val query = uri.query ?: ""
            val params = query.split("&").associate {
                val pair = it.split("=")
                pair[0] to pair.getOrNull(1)
            }
            val token = params["token"]
            if (token != null) {
                PaymentEventBus.publish(PaymentResult(token))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
