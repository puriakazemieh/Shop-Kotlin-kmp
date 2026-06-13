package com.kazemieh.domain.order

import com.kazemieh.common.AppResult

class TrackOrderUseCase(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(id: Long): AppResult<OrderTracking> {
        return repository.trackOrder(id)
    }
}
