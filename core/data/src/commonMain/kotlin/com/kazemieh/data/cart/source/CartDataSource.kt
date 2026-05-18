package com.kazemieh.data.cart.source

import com.kazemieh.common.AppResult
import com.kazemieh.network.dto.cart.request.*
import com.kazemieh.network.dto.cart.response.CartResponse

interface CartDataSource {
    suspend fun getCart(): AppResult<CartResponse>
    suspend fun addItem(request: AddCartItemRequest): AppResult<CartResponse>
    suspend fun updateQty(itemId: Long, request: UpdateCartItemRequest): AppResult<CartResponse>
    suspend fun remove(itemId: Long): AppResult<CartResponse>
    suspend fun clear(): AppResult<Unit>
    suspend fun setVariantQty(variantId: Long, request: SetCartVariantQtyRequest): AppResult<CartResponse>
    suspend fun adjustVariantQty(variantId: Long, request: AdjustCartVariantQtyRequest): AppResult<CartResponse>
}
