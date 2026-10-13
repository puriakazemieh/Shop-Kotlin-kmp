package com.kazemieh.data.local

import com.russhwolf.settings.Settings
import kotlinx.browser.window

private class BrowserSettings : Settings {
    private val prefix = "carmilla.settings."
    private val storage get() = window.localStorage
    private fun storageKey(key: String) = prefix + key
    private fun put(key: String, value: String) = storage.setItem(storageKey(key), value)
    private fun get(key: String): String? = storage.getItem(storageKey(key))

    override val keys: Set<String>
        get() = (0 until storage.length).mapNotNull { index ->
            storage.key(index)?.removePrefix(prefix)?.takeIf { storage.key(index)?.startsWith(prefix) == true }
        }.toSet()
    override val size: Int get() = keys.size

    override fun clear() = keys.forEach(::remove)
    override fun remove(key: String) { storage.removeItem(storageKey(key)) }
    override fun hasKey(key: String): Boolean = storage.getItem(storageKey(key)) != null

    override fun putInt(key: String, value: Int) = put(key, value.toString())
    override fun getInt(key: String, defaultValue: Int): Int = get(key)?.toIntOrNull() ?: defaultValue
    override fun getIntOrNull(key: String): Int? = get(key)?.toIntOrNull()

    override fun putLong(key: String, value: Long) = put(key, value.toString())
    override fun getLong(key: String, defaultValue: Long): Long = get(key)?.toLongOrNull() ?: defaultValue
    override fun getLongOrNull(key: String): Long? = get(key)?.toLongOrNull()

    override fun putString(key: String, value: String) = put(key, value)
    override fun getString(key: String, defaultValue: String): String = get(key) ?: defaultValue
    override fun getStringOrNull(key: String): String? = get(key)

    override fun putFloat(key: String, value: Float) = put(key, value.toString())
    override fun getFloat(key: String, defaultValue: Float): Float = get(key)?.toFloatOrNull() ?: defaultValue
    override fun getFloatOrNull(key: String): Float? = get(key)?.toFloatOrNull()

    override fun putDouble(key: String, value: Double) = put(key, value.toString())
    override fun getDouble(key: String, defaultValue: Double): Double = get(key)?.toDoubleOrNull() ?: defaultValue
    override fun getDoubleOrNull(key: String): Double? = get(key)?.toDoubleOrNull()

    override fun putBoolean(key: String, value: Boolean) = put(key, value.toString())
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = get(key)?.toBooleanStrictOrNull() ?: defaultValue
    override fun getBooleanOrNull(key: String): Boolean? = get(key)?.toBooleanStrictOrNull()
}

actual fun createSettings(): Settings {
    return BrowserSettings()
}
