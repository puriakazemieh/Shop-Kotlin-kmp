package com.kazemieh.home.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppLanguage
import com.kazemieh.domain.usecase.settings.ObserveLanguageUseCase
import com.kazemieh.domain.usecase.settings.UpdateLanguageUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    observeLanguageUseCase: ObserveLanguageUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase
) : ViewModel() {

    val language: StateFlow<AppLanguage> = observeLanguageUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppLanguage.ENGLISH
        )

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch {
            updateLanguageUseCase(language)
        }
    }
}
