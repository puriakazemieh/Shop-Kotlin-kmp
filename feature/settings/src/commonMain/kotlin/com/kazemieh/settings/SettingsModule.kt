package com.kazemieh.settings

import com.kazemieh.domain.settings.ObserveLanguageUseCase
import com.kazemieh.domain.settings.ObserveThemeModeUseCase
import com.kazemieh.domain.settings.UpdateLanguageUseCase
import com.kazemieh.domain.settings.UpdateThemeModeUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    // Settings UseCases
    factory { ObserveLanguageUseCase(get()) }
    factory { UpdateLanguageUseCase(get()) }
    factory { ObserveThemeModeUseCase(get()) }
    factory { UpdateThemeModeUseCase(get()) }

    viewModel {
        SettingsViewModel(
            observeLanguageUseCase = get(),
            updateLanguageUseCase = get(),
            observeThemeModeUseCase = get(),
            updateThemeModeUseCase = get()
        )
    }
}
