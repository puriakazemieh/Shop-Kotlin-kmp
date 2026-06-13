package com.kazemieh.domain.cart

import com.kazemieh.common.AppResult
import com.kazemieh.domain.cart.Cart
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCart(): Flow<AppResult<Cart>>
    suspend fun addItem(variantId: Long, qty: Int): AppResult<Cart>
    suspend fun updateQty(itemId: Long, qty: Int): AppResult<Cart>
    suspend fun remove(itemId: Long): AppResult<Cart>
    suspend fun clear(): AppResult<Unit>
    suspend fun setVariantQty(variantId: Long, qty: Int): AppResult<Cart>
    suspend fun adjustVariantQty(variantId: Long, delta: Int): AppResult<Cart>
    suspend fun moveToSaveForLater(itemId: Long): AppResult<Cart>
    suspend fun moveToCart(itemId: Long): AppResult<Cart>
    suspend fun applyDiscount(code: String): AppResult<Cart>
    suspend fun removeDiscount(): AppResult<Cart>
}
