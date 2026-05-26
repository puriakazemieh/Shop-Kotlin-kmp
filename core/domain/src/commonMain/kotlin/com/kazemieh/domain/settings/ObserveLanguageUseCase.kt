package com.kazemieh.domain.settings

import com.kazemieh.common.AppLanguage
import com.kazemieh.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveLanguageUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<AppLanguage> = repository.observeLanguage()
}
