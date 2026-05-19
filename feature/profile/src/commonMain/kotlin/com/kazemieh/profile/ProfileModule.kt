package com.kazemieh.profile

import com.kazemieh.domain.usecase.GetProfileUseCase
import com.kazemieh.domain.usecase.ObserveProfileUseCase
import com.kazemieh.domain.usecase.UpdateProfileUseCase
import com.kazemieh.domain.usecase.address.AddAddressUseCase
import com.kazemieh.domain.usecase.address.DeleteAddressUseCase
import com.kazemieh.domain.usecase.address.GetAddressesUseCase
import com.kazemieh.domain.usecase.address.SetDefaultAddressUseCase
import com.kazemieh.domain.usecase.address.UpdateAddressUseCase
import com.kazemieh.domain.validation.ValidateProfileUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {

    // ViewModel
    viewModel {
        ProfileViewModel(
            getProfileUseCase = get(),
            updateProfileUseCase = get(),
            observeProfileUseCase = get(),
            validateProfileUseCase = get(),
            getAddressesUseCase = get(),
            addAddressUseCase = get(),
            updateAddressUseCase = get(),
            deleteAddressUseCase = get(),
            setDefaultAddressUseCase = get(),
        )
    }

    // UseCases
    factory { GetProfileUseCase(get()) }
    factory { UpdateProfileUseCase(get()) }
    factory { ObserveProfileUseCase(get()) }

    factory { GetAddressesUseCase(get()) }
    factory { AddAddressUseCase(get()) }
    factory { UpdateAddressUseCase(get()) }
    factory { DeleteAddressUseCase(get()) }
    factory { SetDefaultAddressUseCase(get()) }

    // Validation
    factory { ValidateProfileUseCase() }
}