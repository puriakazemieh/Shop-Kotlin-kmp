package com.kazemieh.home.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppLanguage
import com.kazemieh.common.AppThemeMode
import com.kazemieh.domain.usecase.settings.ObserveLanguageUseCase
import com.kazemieh.domain.usecase.settings.ObserveThemeModeUseCase
import com.kazemieh.domain.usecase.settings.UpdateLanguageUseCase
import com.kazemieh.domain.usecase.settings.UpdateThemeModeUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    observeLanguageUseCase: ObserveLanguageUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase,
    observeThemeModeUseCase: ObserveThemeModeUseCase,
    private val updateThemeModeUseCase: UpdateThemeModeUseCase
) : ViewModel() {

    val language: StateFlow<AppLanguage> = observeLanguageUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppLanguage.ENGLISH
        )

    val themeMode: StateFlow<AppThemeMode> = observeThemeModeUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppThemeMode.SYSTEM
        )

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch {
            updateLanguageUseCase(language)
        }
    }

    fun onThemeModeSelected(mode: AppThemeMode) {
        viewModelScope.launch {
            updateThemeModeUseCase(mode)
        }
    }
}
