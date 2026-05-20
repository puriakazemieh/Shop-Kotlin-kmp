package com.kazemieh.domain.repository

import com.kazemieh.common.AppLanguage
import com.kazemieh.common.AppThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
    suspend fun getLanguage(): AppLanguage

    fun observeThemeMode(): Flow<AppThemeMode>
    suspend fun setThemeMode(mode: AppThemeMode)
    suspend fun getThemeMode(): AppThemeMode
}
