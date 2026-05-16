package com.kazemieh.home

import com.kazemieh.domain.usecase.IsUserLoggedInUseCase
import com.kazemieh.domain.usecase.ObserveAuthStateUseCase
import com.kazemieh.domain.usecase.ObserveProfileUseCase
import com.kazemieh.domain.usecase.SignOutUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {

    // ViewModel
    viewModel {
        HomeGraphViewModel(
            isUserLoggedInUseCase = get(),
            signOutUseCase = get(),
            observeAuthStateUseCase = get(),
            observeProfileUseCase = get()
        )
    }

    // UseCases
    factory { IsUserLoggedInUseCase(get()) }
    factory { SignOutUseCase(get()) }
    factory { ObserveAuthStateUseCase(get()) }

}