package com.kazemieh.orders.di

import com.kazemieh.domain.order.CancelOrderUseCase
import com.kazemieh.domain.order.CreateReturnRequestUseCase
import com.kazemieh.domain.order.GetMyOrdersUseCase
import com.kazemieh.domain.order.GetOrderUseCase
import com.kazemieh.domain.order.ListMyReturnRequestsUseCase
import com.kazemieh.domain.order.ReorderUseCase
import com.kazemieh.domain.order.TrackOrderUseCase
import com.kazemieh.orders.detail.OrderDetailViewModel
import com.kazemieh.orders.list.OrderListViewModel
import com.kazemieh.orders.returns.ReturnRequestViewModel
import com.kazemieh.orders.tracking.OrderTrackingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val ordersModule = module {
    factory { GetMyOrdersUseCase(get()) }
    factory { GetOrderUseCase(get()) }
    factory { CancelOrderUseCase(get()) }
    factory { TrackOrderUseCase(get()) }
    factory { ReorderUseCase(get()) }
    factory { CreateReturnRequestUseCase(get()) }
    factory { ListMyReturnRequestsUseCase(get()) }

    viewModel { OrderListViewModel(get()) }
    viewModel { OrderDetailViewModel(get(), get(), get()) }
    viewModel { OrderTrackingViewModel(get()) }
    viewModel { ReturnRequestViewModel(get(), get()) }
}
