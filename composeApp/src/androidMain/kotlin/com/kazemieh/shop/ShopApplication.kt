package com.kazemieh.shop


import android.app.Application
import com.kazemieh.common.isDebugLoggingEnabled
import com.kazemieh.designsystem.brand.BrandConfig
import com.kazemieh.designsystem.brand.BrandRegistry
import org.koin.android.ext.koin.androidContext

class ShopApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        isDebugLoggingEnabled = BuildConfig.DEBUG

        // برندِ فعال از فلِیورِ build (BuildConfig.BRAND) انتخاب می‌شود.
        initKoin(brand = resolveBrand()) {
            androidContext(this@ShopApplication)
        }
    }

    /**
     * برندِ فلِیور را برمی‌گرداند؛ اگر هنگامِ بیلد آدرسِ API با
     * `-PcarmillaApiBase=...` تنظیم شده باشد (BuildConfig.API_BASE_OVERRIDE)،
     * همان برند با آدرسِ دلخواه استفاده می‌شود — برای وصل‌کردنِ APK به یک
     * وردپرس/سرورِ مشخص بدونِ تغییرِ کد.
     */
    private fun resolveBrand(): BrandConfig {
        val brand = BrandRegistry.byId(BuildConfig.BRAND)
        val override = BuildConfig.API_BASE_OVERRIDE
        return if (override.isNotBlank()) brand.copy(apiBaseUrl = override) else brand
    }
}
