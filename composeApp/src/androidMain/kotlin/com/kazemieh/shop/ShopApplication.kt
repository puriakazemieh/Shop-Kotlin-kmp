package com.kazemieh.shop


import android.app.Application
import com.kazemieh.designsystem.brand.BrandRegistry
import org.koin.android.ext.koin.androidContext

class ShopApplication : Application() {


    override fun onCreate() {
        super.onCreate()

        // برندِ فعال از فلِیورِ build (BuildConfig.BRAND) انتخاب می‌شود.
        initKoin(brand = BrandRegistry.byId(BuildConfig.BRAND)) {
            androidContext(this@ShopApplication)
        }
    }
}