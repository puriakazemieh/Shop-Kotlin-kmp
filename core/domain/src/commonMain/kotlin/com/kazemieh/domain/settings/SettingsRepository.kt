package com.kazemieh.domain.settings

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

    // Recent searches (local history, most-recent-first)
    suspend fun getRecentSearches(): List<String>
    suspend fun addRecentSearch(query: String)
    suspend fun clearRecentSearches()
}
