package com.kazemieh.domain.settings

class GetRecentSearchesUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(): List<String> = repository.getRecentSearches()
}

class AddRecentSearchUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(query: String) = repository.addRecentSearch(query)
}

class ClearRecentSearchesUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke() = repository.clearRecentSearches()
}
