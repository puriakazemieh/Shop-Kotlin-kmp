package com.kazemieh.profile

import com.kazemieh.domain.profile.GetProfileUseCase
import com.kazemieh.domain.profile.ObserveProfileUseCase
import com.kazemieh.domain.profile.UpdateProfileUseCase
import com.kazemieh.domain.address.AddAddressUseCase
import com.kazemieh.domain.address.DeleteAddressUseCase
import com.kazemieh.domain.address.GetAddressesUseCase
import com.kazemieh.domain.address.SetDefaultAddressUseCase
import com.kazemieh.domain.address.UpdateAddressUseCase
import com.kazemieh.domain.profile.ValidateProfileUseCase
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
