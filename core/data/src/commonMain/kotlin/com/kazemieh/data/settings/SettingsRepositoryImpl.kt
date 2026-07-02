package com.kazemieh.data.settings

import com.kazemieh.network.common.*
import com.kazemieh.common.*
import com.kazemieh.domain.settings.SettingsRepository
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toFlowSettings
import com.russhwolf.settings.observable.makeObservable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(private val settings: Settings) : SettingsRepository {
    @OptIn(ExperimentalSettingsApi::class)
    private val flowSettings = ((settings as? ObservableSettings) ?: settings.makeObservable()).toFlowSettings()

    @OptIn(ExperimentalSettingsApi::class)
    override fun observeLanguage(): Flow<AppLanguage> {
        return flowSettings.getStringFlow(KEY_LANGUAGE, AppLanguage.ENGLISH.code)
            .map { AppLanguage.fromCode(it) }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        settings.putString(KEY_LANGUAGE, language.code)
    }

    override suspend fun getLanguage(): AppLanguage {
        val code = settings.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code)
        return AppLanguage.fromCode(code)
    }

    @OptIn(ExperimentalSettingsApi::class)
    override fun observeThemeMode(): Flow<AppThemeMode> {
        return flowSettings.getStringFlow(KEY_THEME_MODE, AppThemeMode.LIGHT.code)
            .map { AppThemeMode.fromCode(it) }
    }

    override suspend fun setThemeMode(mode: AppThemeMode) {
        settings.putString(KEY_THEME_MODE, mode.code)
    }

    override suspend fun getThemeMode(): AppThemeMode {
        val code = settings.getString(KEY_THEME_MODE, AppThemeMode.LIGHT.code)
        return AppThemeMode.fromCode(code)
    }

    override suspend fun getRecentSearches(): List<String> {
        val raw = settings.getString(KEY_RECENT_SEARCHES, "")
        return raw.split(RECENT_DELIMITER).map { it.trim() }.filter { it.isNotEmpty() }
    }

    override suspend fun addRecentSearch(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        val current = getRecentSearches().toMutableList()
        current.removeAll { it.equals(q, ignoreCase = true) }
        current.add(0, q)
        val trimmed = current.take(MAX_RECENT_SEARCHES)
        settings.putString(KEY_RECENT_SEARCHES, trimmed.joinToString(RECENT_DELIMITER))
    }

    override suspend fun clearRecentSearches() {
        settings.putString(KEY_RECENT_SEARCHES, "")
    }

    companion object {
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_THEME_MODE = "app_theme_mode"
        private const val KEY_RECENT_SEARCHES = "recent_searches"
        private const val RECENT_DELIMITER = "\n"
        private const val MAX_RECENT_SEARCHES = 8
    }
}
