package com.kazemieh.data.repository

import com.kazemieh.common.AppLanguage
import com.kazemieh.common.AppThemeMode
import com.kazemieh.domain.repository.SettingsRepository
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
        return flowSettings.getStringFlow(KEY_THEME_MODE, AppThemeMode.SYSTEM.code)
            .map { AppThemeMode.fromCode(it) }
    }

    override suspend fun setThemeMode(mode: AppThemeMode) {
        settings.putString(KEY_THEME_MODE, mode.code)
    }

    override suspend fun getThemeMode(): AppThemeMode {
        val code = settings.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.code)
        return AppThemeMode.fromCode(code)
    }

    companion object {
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_THEME_MODE = "app_theme_mode"
    }
}
