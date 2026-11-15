package com.kazemieh.data.order.repository

import com.kazemieh.common.AppResult
import com.kazemieh.config.capabilities.FeatureUseCaseGuard
import com.kazemieh.data.common.guardOrError
import com.kazemieh.domain.order.RecurringOrder
import com.kazemieh.domain.order.RecurringOrderRepository
import com.kazemieh.network.common.safeApiCall
import com.kazemieh.network.order.CreateRecurringOrderRequest
import com.kazemieh.network.order.RecurringOrderApi
import com.kazemieh.network.order.RecurringOrderResponse

class RecurringOrderRepositoryImpl(
    private val api: RecurringOrderApi,
    private val guard: FeatureUseCaseGuard? = null
) : RecurringOrderRepository {

    override suspend fun create(variantId: Long, qty: Int, addressId: Long?, intervalDays: Int): AppResult<RecurringOrder> =
        guard.guardOrError("commerce.core") {
            safeApiCall {
                api.create(CreateRecurringOrderRequest(variantId, qty, addressId, intervalDays)).toDomain()
            }
        }

    override suspend fun listMine(): AppResult<List<RecurringOrder>> =
        guard.guardOrError("commerce.core") {
            safeApiCall {
                api.listMine().map { it.toDomain() }
            }
        }

    override suspend fun cancel(id: Long): AppResult<Unit> =
        guard.guardOrError("commerce.core") {
            safeApiCall {
                api.cancel(id)
            }
        }

    private fun RecurringOrderResponse.toDomain() = RecurringOrder(
        id = id, variantId = variantId, qty = qty, intervalDays = intervalDays,
        nextRunAt = nextRunAt, isActive = isActive, lastOrderId = lastOrderId
    )
}
