package com.kazemieh.admin.orders

import com.kazemieh.domain.admin.GetAdminOrderDetailUseCase
import com.kazemieh.domain.admin.ListAdminOrdersUseCase
import com.kazemieh.domain.admin.UpdateAdminOrderStatusUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adminOrdersModule = module {
    // Admin Order UseCases
    factory { ListAdminOrdersUseCase(get()) }
    factory { GetAdminOrderDetailUseCase(get()) }
    factory { UpdateAdminOrderStatusUseCase(get()) }

    viewModel {
        AdminOrderViewModel(
            listAdminOrdersUseCase = get(),
            getAdminOrderDetailUseCase = get(),
            updateAdminOrderStatusUseCase = get()
        )
    }
}
