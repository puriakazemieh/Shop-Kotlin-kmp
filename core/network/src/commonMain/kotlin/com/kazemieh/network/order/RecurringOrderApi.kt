package com.kazemieh.network.order

import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

@Serializable
data class CreateRecurringOrderRequest(
    val variantId: Long,
    val qty: Int = 1,
    val addressId: Long? = null,
    val intervalDays: Int = 30
)

@Serializable
data class RecurringOrderResponse(
    val id: Long,
    val variantId: Long,
    val qty: Int,
    val intervalDays: Int,
    val nextRunAt: String,
    val isActive: Boolean,
    val lastOrderId: Long? = null
)

interface RecurringOrderApi {
    suspend fun create(request: CreateRecurringOrderRequest): RecurringOrderResponse
    suspend fun listMine(): List<RecurringOrderResponse>
    suspend fun cancel(id: Long)
}

class RecurringOrderApiImpl(private val client: HttpClient) : RecurringOrderApi {
    override suspend fun create(request: CreateRecurringOrderRequest): RecurringOrderResponse = safeApiCallRaw {
        client.post("api/recurring-orders") { setBody(request) }
    }

    override suspend fun listMine(): List<RecurringOrderResponse> = safeApiCallRaw {
        client.get("api/recurring-orders/mine")
    }

    override suspend fun cancel(id: Long): Unit = safeApiCallRaw {
        client.post("api/recurring-orders/$id/cancel")
    }
}
