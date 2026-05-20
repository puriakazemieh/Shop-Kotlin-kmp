package com.kazemieh.data.local

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.observable.makeObservable

@OptIn(ExperimentalSettingsApi::class)
actual fun createSettings(): Settings {
    return StorageSettings().makeObservable()
}
