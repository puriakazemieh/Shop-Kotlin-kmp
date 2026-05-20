package com.kazemieh.domain.repository

import com.kazemieh.common.AppLanguage
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
    suspend fun getLanguage(): AppLanguage
}
