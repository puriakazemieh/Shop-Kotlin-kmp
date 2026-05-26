package com.kazemieh.domain.settings

import com.kazemieh.common.AppLanguage

class UpdateLanguageUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(language: AppLanguage) = repository.setLanguage(language)
}
