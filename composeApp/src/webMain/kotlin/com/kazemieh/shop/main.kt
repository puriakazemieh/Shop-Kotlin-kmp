package com.kazemieh.shop

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.kazemieh.common.PaymentEventBus
import com.kazemieh.common.PaymentResult
import com.kazemieh.designsystem.brand.BrandRegistry
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin(resolveBrand())
    handleWebDeepLink()
    ComposeViewport {
        App()
    }
}

/**
 * برندِ فعال را از پارامترهای URL انتخاب می‌کند (افزودنی و اختیاری):
 *  - `?brand=wp` → برندِ وردپرس.
 *  - `?api=<baseUrl>` → همان برندِ wp اما با آدرسِ API دلخواه (برای اتصالِ وبِ
 *    کاتلین به یک سایتِ وردپرسِ مشخص، مثلاً در محیطِ CI برای مقایسه‌ی هم‌دیتا).
 * اگر هیچ‌کدام نبود، برندِ پیش‌فرض (Carmila → سرورِ فعلی) بدونِ تغییر می‌ماند.
 */
private fun resolveBrand(): com.kazemieh.designsystem.brand.BrandConfig {
    val params = URLSearchParams(window.location.search)
    val isLocalhost = window.location.hostname == "localhost" || window.location.hostname == "127.0.0.1"
    val api = if (isLocalhost) params.get("api")?.takeIf { it.isNotBlank() } else null
    val brandId = params.get("brand")?.takeIf { it.isNotBlank() }
    val base = when {
        brandId != null -> BrandRegistry.byId(brandId)
        api != null -> BrandRegistry.byId("wp")
        else -> BrandRegistry.default
    } ?: BrandRegistry.default
    return if (api != null) base.copy(apiBaseUrl = api) else base
}

private fun handleWebDeepLink() {
    val searchParams = URLSearchParams(window.location.search)
    val token = searchParams.get("token")
    
    if (token != null) {
        PaymentEventBus.publish(PaymentResult(token))
    }
}
