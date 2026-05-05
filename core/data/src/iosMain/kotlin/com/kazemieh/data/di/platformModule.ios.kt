package com.kazemieh.data.di

import com.kazemieh.data.local.createSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<Settings> {
        createSettings()
    }
}