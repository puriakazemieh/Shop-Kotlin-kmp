package com.kazemieh.domain.usecase.settings

import com.kazemieh.common.AppLanguage
import com.kazemieh.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveLanguageUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<AppLanguage> = repository.observeLanguage()
}
