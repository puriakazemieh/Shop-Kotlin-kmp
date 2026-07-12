package com.kazemieh.admin.psychtest

import com.kazemieh.domain.psychtest.CreatePsychTestUseCase
import com.kazemieh.domain.psychtest.DeletePsychTestUseCase
import com.kazemieh.domain.psychtest.GetAdminPsychTestsUseCase
import com.kazemieh.domain.psychtest.GetPendingInterpretationsUseCase
import com.kazemieh.domain.psychtest.InterpretTestUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adminPsychTestModule = module {
    factory { GetAdminPsychTestsUseCase(get()) }
    factory { CreatePsychTestUseCase(get()) }
    factory { DeletePsychTestUseCase(get()) }
    factory { GetPendingInterpretationsUseCase(get()) }
    factory { InterpretTestUseCase(get()) }

    viewModel {
        AdminPsychTestViewModel(
            getAdminPsychTestsUseCase = get(),
            createPsychTestUseCase = get(),
            deletePsychTestUseCase = get(),
            getPendingInterpretationsUseCase = get(),
            interpretTestUseCase = get()
        )
    }
}
