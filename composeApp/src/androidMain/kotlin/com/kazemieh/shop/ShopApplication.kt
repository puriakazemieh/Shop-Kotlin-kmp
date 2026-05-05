package com.kazemieh.shop


import android.app.Application
import org.koin.android.ext.koin.androidContext

class ShopApplication : Application() {


    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@ShopApplication)
        }
    }
}