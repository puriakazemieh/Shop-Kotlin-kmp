package com.kazemieh.data.cart.source

import com.kazemieh.network.cart.dto.request.*
import com.kazemieh.network.cart.dto.response.*
import com.kazemieh.domain.cart.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*




interface CartDataSource {
    suspend fun getCart(): AppResult<CartResponse>
    suspend fun addItem(request: AddCartItemRequest): AppResult<CartResponse>
    suspend fun updateQty(itemId: Long, request: UpdateCartItemRequest): AppResult<CartResponse>
    suspend fun remove(itemId: Long): AppResult<CartResponse>
    suspend fun clear(): AppResult<Unit>
    suspend fun setVariantQty(variantId: Long, request: SetCartVariantQtyRequest): AppResult<CartResponse>
    suspend fun adjustVariantQty(variantId: Long, request: AdjustCartVariantQtyRequest): AppResult<CartResponse>
    suspend fun moveToSaveForLater(itemId: Long): AppResult<CartResponse>
    suspend fun moveToCart(itemId: Long): AppResult<CartResponse>
    suspend fun applyDiscount(request: ApplyDiscountRequest): AppResult<CartResponse>
    suspend fun removeDiscount(): AppResult<CartResponse>
}
