package com.kazemieh.main

import com.kazemieh.domain.auth.ObserveAuthStateUseCase
import com.kazemieh.domain.auth.ResetPasswordUseCase
import com.kazemieh.domain.auth.SignOutUseCase
import com.kazemieh.domain.cart.GetCartUseCase
import com.kazemieh.domain.profile.GetProfileUseCase
import com.kazemieh.domain.profile.ObserveProfileUseCase
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


    factory { SignOutUseCase(get()) }
    factory { ObserveAuthStateUseCase(get()) }
    factory { ObserveProfileUseCase(get()) }
    factory { GetProfileUseCase(get()) }
    factory { GetCartUseCase(get()) }
}
