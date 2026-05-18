package com.kazemieh.network

import com.kazemieh.network.dto.cart.request.*
import com.kazemieh.network.dto.cart.response.*

interface CartApi {
    suspend fun getCart(): CartResponse
    suspend fun addItem(request: AddCartItemRequest): CartResponse
    suspend fun updateQty(itemId: Long, request: UpdateCartItemRequest): CartResponse
    suspend fun remove(itemId: Long): CartResponse
    suspend fun clear()
    suspend fun setVariantQty(variantId: Long, request: SetCartVariantQtyRequest): CartResponse
    suspend fun adjustVariantQty(variantId: Long, request: AdjustCartVariantQtyRequest): CartResponse
}
