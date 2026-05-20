package com.kazemieh.domain.usecase.settings

import com.kazemieh.common.AppLanguage
import com.kazemieh.domain.repository.SettingsRepository

class UpdateLanguageUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(language: AppLanguage) = repository.setLanguage(language)
}
