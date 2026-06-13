package com.kazemieh.network.cart

import com.kazemieh.network.cart.dto.request.*
import com.kazemieh.network.cart.dto.response.*

import com.kazemieh.network.common.PageResponse

import com.kazemieh.network.cart.dto.*
import com.kazemieh.network.cart.dto.*

interface CartApi {
    suspend fun getCart(): CartResponse
    suspend fun addItem(request: AddCartItemRequest): CartResponse
    suspend fun updateQty(itemId: Long, request: UpdateCartItemRequest): CartResponse
    suspend fun remove(itemId: Long): CartResponse
    suspend fun clear()
    suspend fun setVariantQty(variantId: Long, request: SetCartVariantQtyRequest): CartResponse
    suspend fun adjustVariantQty(variantId: Long, request: AdjustCartVariantQtyRequest): CartResponse
    suspend fun moveToSaveForLater(itemId: Long): CartResponse
    suspend fun moveToCart(itemId: Long): CartResponse
}
