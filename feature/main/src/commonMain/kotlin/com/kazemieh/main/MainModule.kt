package com.kazemieh.main

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    viewModel {
        MainViewModel(
            signOutUseCase = get(),
            observeAuthStateUseCase = get(),
            observeProfileUseCase = get(),
            getProfileUseCase = get(),
            getCartUseCase = get()
        )
    }
}
