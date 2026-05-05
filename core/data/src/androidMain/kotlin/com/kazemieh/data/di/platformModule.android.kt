package com.kazemieh.data.di

import android.content.Context
import com.kazemieh.data.local.createSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

actual fun platformModule(): Module = module {
    val context = KoinPlatform.getKoin().get<Context>()
    single<Settings> {
        createSettings(context)
    }
}