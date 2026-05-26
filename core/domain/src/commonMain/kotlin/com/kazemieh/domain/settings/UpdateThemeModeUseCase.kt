package com.kazemieh.domain.settings

import com.kazemieh.common.AppThemeMode
import com.kazemieh.domain.settings.SettingsRepository

class UpdateThemeModeUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(mode: AppThemeMode) = repository.setThemeMode(mode)
}
