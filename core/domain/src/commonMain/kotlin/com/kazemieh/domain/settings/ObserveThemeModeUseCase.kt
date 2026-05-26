package com.kazemieh.domain.settings

import com.kazemieh.common.AppThemeMode
import com.kazemieh.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveThemeModeUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<AppThemeMode> = repository.observeThemeMode()
}
