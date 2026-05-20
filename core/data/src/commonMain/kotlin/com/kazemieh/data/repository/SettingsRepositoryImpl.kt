package com.kazemieh.data.repository

import com.kazemieh.common.AppLanguage
import com.kazemieh.domain.repository.SettingsRepository
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(private val settings: Settings) : SettingsRepository {
    @OptIn(ExperimentalSettingsApi::class)
    private val flowSettings = (settings as ObservableSettings).toFlowSettings()

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

    companion object {
        private const val KEY_LANGUAGE = "app_language"
    }
}
