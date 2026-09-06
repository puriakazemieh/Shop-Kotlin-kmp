package com.kazemieh.shop

import android.app.Application
import com.kazemieh.shop.app.BuildConfig
import com.kazemieh.common.isDebugLoggingEnabled
import org.koin.android.ext.koin.androidContext

class ShopApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        isDebugLoggingEnabled = BuildConfig.DEBUG

        initKoin(
            sku = BuildConfig.BRAND,
            apiBaseUrlOverride = BuildConfig.API_BASE_OVERRIDE.takeIf { it.isNotBlank() }
        ) {
            androidContext(this@ShopApplication)
        }
    }
}
