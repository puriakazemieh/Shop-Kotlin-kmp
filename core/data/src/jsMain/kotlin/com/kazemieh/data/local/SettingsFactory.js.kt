package com.kazemieh.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

actual fun createSettings(): Settings {
    return StorageSettings()
}