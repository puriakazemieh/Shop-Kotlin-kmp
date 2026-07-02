package com.kazemieh.support

import com.kazemieh.domain.support.CreateTicketUseCase
import com.kazemieh.domain.support.GetTicketDetailUseCase
import com.kazemieh.domain.support.GetTicketsUseCase
import com.kazemieh.domain.support.PostSupportMessageUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val supportModule = module {
    factory { GetTicketsUseCase(get()) }
    factory { GetTicketDetailUseCase(get()) }
    factory { CreateTicketUseCase(get()) }
    factory { PostSupportMessageUseCase(get()) }

    viewModel {
        SupportViewModel(
            getTicketsUseCase = get(),
            getTicketDetailUseCase = get(),
            createTicketUseCase = get(),
            postSupportMessageUseCase = get()
        )
    }
}
