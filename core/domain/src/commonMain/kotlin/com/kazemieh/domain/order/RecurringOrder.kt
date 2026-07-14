package com.kazemieh.domain.order

import com.kazemieh.common.AppResult

data class RecurringOrder(
    val id: Long,
    val variantId: Long,
    val qty: Int,
    val intervalDays: Int,
    val nextRunAt: String,
    val isActive: Boolean,
    val lastOrderId: Long?
)

interface RecurringOrderRepository {
    suspend fun create(variantId: Long, qty: Int, addressId: Long?, intervalDays: Int): AppResult<RecurringOrder>
    suspend fun listMine(): AppResult<List<RecurringOrder>>
    suspend fun cancel(id: Long): AppResult<Unit>
}

class CreateRecurringOrderUseCase(private val repository: RecurringOrderRepository) {
    suspend operator fun invoke(variantId: Long, qty: Int, addressId: Long?, intervalDays: Int): AppResult<RecurringOrder> =
        repository.create(variantId, qty, addressId, intervalDays)
}

class ListMyRecurringOrdersUseCase(private val repository: RecurringOrderRepository) {
    suspend operator fun invoke(): AppResult<List<RecurringOrder>> = repository.listMine()
}

class CancelRecurringOrderUseCase(private val repository: RecurringOrderRepository) {
    suspend operator fun invoke(id: Long): AppResult<Unit> = repository.cancel(id)
}
