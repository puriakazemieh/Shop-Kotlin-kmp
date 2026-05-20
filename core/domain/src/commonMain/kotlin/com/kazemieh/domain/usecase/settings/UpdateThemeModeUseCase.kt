package com.kazemieh.domain.usecase.settings

import com.kazemieh.common.AppThemeMode
import com.kazemieh.domain.repository.SettingsRepository

class UpdateThemeModeUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(mode: AppThemeMode) = repository.setThemeMode(mode)
}
