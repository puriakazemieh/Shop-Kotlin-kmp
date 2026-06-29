package com.kazemieh.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppLanguage
import com.kazemieh.common.AppThemeMode
import com.kazemieh.domain.settings.ObserveLanguageUseCase
import com.kazemieh.domain.settings.ObserveThemeModeUseCase
import com.kazemieh.domain.settings.UpdateLanguageUseCase
import com.kazemieh.domain.settings.UpdateThemeModeUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    observeLanguageUseCase: ObserveLanguageUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase,
    observeThemeModeUseCase: ObserveThemeModeUseCase,
    private val updateThemeModeUseCase: UpdateThemeModeUseCase
) : ViewModel() {

    val state: StateFlow<SettingsState> = combine(
        observeLanguageUseCase(),
        observeThemeModeUseCase()
    ) { language, themeMode ->
        SettingsState(language, themeMode)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState()
    )

    fun handleIntent(intent: SettingsIntent) {
        viewModelScope.launch {
            when (intent) {
                is SettingsIntent.SelectLanguage -> updateLanguageUseCase(intent.language)
                is SettingsIntent.SelectThemeMode -> updateThemeModeUseCase(intent.mode)
            }
        }
    }
}

data class SettingsState(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT
)

sealed interface SettingsIntent {
    data class SelectLanguage(val language: AppLanguage) : SettingsIntent
    data class SelectThemeMode(val mode: AppThemeMode) : SettingsIntent
}
