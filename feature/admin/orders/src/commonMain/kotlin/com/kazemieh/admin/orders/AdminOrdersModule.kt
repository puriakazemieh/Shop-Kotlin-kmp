package com.kazemieh.admin.orders

import com.kazemieh.domain.admin.GetAdminOrderDetailUseCase
import com.kazemieh.domain.admin.ListAdminOrdersUseCase
import com.kazemieh.domain.admin.UpdateAdminOrderStatusUseCase
import com.kazemieh.domain.order.AdminListReturnRequestsUseCase
import com.kazemieh.domain.order.AdminUpdateReturnRequestUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adminOrdersModule = module {
    // Admin Order UseCases
    factory { ListAdminOrdersUseCase(get()) }
    factory { GetAdminOrderDetailUseCase(get()) }
    factory { UpdateAdminOrderStatusUseCase(get()) }
    factory { AdminListReturnRequestsUseCase(get()) }
    factory { AdminUpdateReturnRequestUseCase(get()) }

    viewModel {
        AdminOrderViewModel(
            listAdminOrdersUseCase = get(),
            getAdminOrderDetailUseCase = get(),
            updateAdminOrderStatusUseCase = get()
        )
    }

    viewModel {
        AdminReturnRequestsViewModel(
            adminListReturnRequestsUseCase = get(),
            adminUpdateReturnRequestUseCase = get()
        )
    }
}
