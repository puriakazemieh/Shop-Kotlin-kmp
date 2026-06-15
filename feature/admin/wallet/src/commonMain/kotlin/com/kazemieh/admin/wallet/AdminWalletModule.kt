package com.kazemieh.admin.wallet

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adminWalletModule = module {
    viewModel {
        AdminWalletViewModel(
            searchWalletUsersUseCase = get(),
            adjustWalletUseCase = get()
        )
    }

    viewModel {
        AdminWithdrawalsViewModel(
            listWithdrawalsUseCase = get(),
            processWithdrawalUseCase = get()
        )
    }
}
