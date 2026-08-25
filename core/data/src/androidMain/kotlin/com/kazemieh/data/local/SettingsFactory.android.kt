package com.kazemieh.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings


actual fun createSettings(): Settings {
    throw IllegalStateException("createSettings() needs Context. Use createSettings(context) instead or inject Settings from Koin.")
}

fun createSettings(context: Context): Settings {
    // Plaintext tokens from the legacy store are intentionally discarded so a
    // user must authenticate again rather than migrating secret material.
    context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        .edit()
        .remove("access_token")
        .remove("refresh_token")
        .apply()

    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    val delegate = EncryptedSharedPreferences.create(
        context,
        "secure_profile_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    return SharedPreferencesSettings(delegate)
}
