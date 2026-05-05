package com.kazemieh.data.local

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

actual fun createSettings(): Settings {
    val delegate = Preferences.userRoot().node("com.kazemieh.shop.profile")
    return PreferencesSettings(delegate)
}