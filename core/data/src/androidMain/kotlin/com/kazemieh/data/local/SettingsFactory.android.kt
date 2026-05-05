package com.kazemieh.data.local

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings


actual fun createSettings(): Settings {
    throw IllegalStateException("createSettings() needs Context. Use createSettings(context) instead or inject Settings from Koin.")
}

fun createSettings(context: Context): Settings {
    val delegate = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(delegate)
}