package com.kazemieh.psychtest

import com.kazemieh.domain.psychtest.GetMyPsychTestsUseCase
import com.kazemieh.domain.psychtest.GetPsychTestDetailUseCase
import com.kazemieh.domain.psychtest.GetPsychTestsUseCase
import com.kazemieh.domain.psychtest.GetUserTestQuestionsUseCase
import com.kazemieh.domain.psychtest.SubmitPsychTestUseCase
import com.kazemieh.psychtest.list.PsychTestListViewModel
import com.kazemieh.psychtest.take.TakeTestViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val psychTestModule = module {
    factory { GetPsychTestsUseCase(get()) }
    factory { GetPsychTestDetailUseCase(get()) }
    factory { GetMyPsychTestsUseCase(get()) }
    factory { GetUserTestQuestionsUseCase(get()) }
    factory { SubmitPsychTestUseCase(get()) }

    viewModel { PsychTestListViewModel(get(), get()) }
    viewModel { TakeTestViewModel(get(), get()) }
}
