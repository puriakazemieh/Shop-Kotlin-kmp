package com.kazemieh.admin.options

import com.kazemieh.domain.usecase.admin.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adminOptionsModule = module {
    // Admin Options UseCases
    factory { GetAdminOptionsUseCase(get()) }
    factory { CreateOptionTypeUseCase(get()) }
    factory { UpdateOptionTypeUseCase(get()) }
    factory { DeleteOptionTypeUseCase(get()) }
    factory { CreateOptionValueUseCase(get()) }
    factory { UpdateOptionValueUseCase(get()) }
    factory { DeleteOptionValueUseCase(get()) }

    viewModel {
        ManageOptionsViewModel(
            getAdminOptionsUseCase = get(),
            createOptionTypeUseCase = get(),
            updateOptionTypeUseCase = get(),
            deleteOptionTypeUseCase = get(),
            createOptionValueUseCase = get(),
            updateOptionValueUseCase = get(),
            deleteOptionValueUseCase = get()
        )
    }
}
