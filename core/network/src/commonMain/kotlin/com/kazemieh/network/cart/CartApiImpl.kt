package com.kazemieh.network.cart

import com.kazemieh.network.cart.dto.request.*
import com.kazemieh.network.cart.dto.response.*

import com.kazemieh.network.common.PageResponse

import com.kazemieh.network.common.safeApiCallRaw

import com.kazemieh.network.cart.dto.*
import com.kazemieh.network.cart.dto.*
import io.ktor.client.HttpClient
import io.ktor.client.request.*

class CartApiImpl(
    private val client: HttpClient
) : CartApi {

    override suspend fun getCart(): CartResponse = safeApiCallRaw {
        client.get("api/cart")
    }

    override suspend fun addItem(request: AddCartItemRequest): CartResponse = safeApiCallRaw {
        client.post("api/cart/items") {
            setBody(request)
        }
    }

    override suspend fun updateQty(itemId: Long, request: UpdateCartItemRequest): CartResponse = safeApiCallRaw {
        client.patch("api/cart/items/$itemId") {
            setBody(request)
        }
    }

    override suspend fun remove(itemId: Long): CartResponse = safeApiCallRaw {
        client.delete("api/cart/items/$itemId")
    }

    override suspend fun clear(): Unit = safeApiCallRaw {
        client.delete("api/cart")
    }

    override suspend fun setVariantQty(variantId: Long, request: SetCartVariantQtyRequest): CartResponse = safeApiCallRaw {
        client.put("api/cart/items/$variantId") {
            setBody(request)
        }
    }

    override suspend fun adjustVariantQty(variantId: Long, request: AdjustCartVariantQtyRequest): CartResponse = safeApiCallRaw {
        client.patch("api/cart/items/$variantId/adjust") {
            setBody(request)
        }
    }
}
