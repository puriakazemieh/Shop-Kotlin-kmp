package com.kazemieh.profile

import com.kazemieh.domain.usecase.GetProfileUseCase
import com.kazemieh.domain.usecase.ObserveProfileUseCase
import com.kazemieh.domain.usecase.UpdateProfileUseCase
import com.kazemieh.domain.usecase.ValidateProfileUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {

    // ViewModel
    viewModel {
        ProfileViewModel(
            getProfileUseCase = get(),
            updateProfileUseCase = get(),
            observeProfileUseCase = get(),
            validateProfileUseCase = get()
        )
    }

    // UseCases
    factory { GetProfileUseCase(get()) }
    factory { UpdateProfileUseCase(get()) }
    factory { ObserveProfileUseCase(get()) }

    // Validation
    factory { ValidateProfileUseCase() }
}