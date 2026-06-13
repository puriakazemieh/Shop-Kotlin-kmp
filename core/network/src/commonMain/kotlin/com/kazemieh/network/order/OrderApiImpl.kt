package com.kazemieh.network.order

import com.kazemieh.network.order.dto.request.*
import com.kazemieh.network.order.dto.response.*

import com.kazemieh.network.common.safeApiCallRaw

import io.ktor.client.HttpClient
import io.ktor.client.request.*

class OrderApiImpl(
    private val client: HttpClient
) : OrderApi {

    override suspend fun listMyOrders(): List<OrderResponse> = safeApiCallRaw {
        client.get("api/orders")
    }

    override suspend fun getOrder(id: Long): OrderDetailResponse = safeApiCallRaw {
        client.get("api/orders/$id")
    }

    override suspend fun createOrder(request: CreateOrderRequest): OrderDetailResponse = safeApiCallRaw {
        client.post("api/orders") {
            setBody(request)
        }
    }

    override suspend fun cancelOrder(id: Long): Unit = safeApiCallRaw {
        client.post("api/orders/$id/cancel")
    }

    override suspend fun trackOrder(id: Long): OrderTrackingResponse = safeApiCallRaw {
        client.get("api/orders/$id/track")
    }
}
