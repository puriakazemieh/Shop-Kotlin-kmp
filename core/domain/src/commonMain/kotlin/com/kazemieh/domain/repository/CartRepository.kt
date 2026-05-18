package com.kazemieh.domain.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Cart
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCart(): Flow<AppResult<Cart>>
    suspend fun addItem(variantId: Long, qty: Int): AppResult<Cart>
    suspend fun updateQty(itemId: Long, qty: Int): AppResult<Cart>
    suspend fun remove(itemId: Long): AppResult<Cart>
    suspend fun clear(): AppResult<Unit>
    suspend fun setVariantQty(variantId: Long, qty: Int): AppResult<Cart>
    suspend fun adjustVariantQty(variantId: Long, delta: Int): AppResult<Cart>
}
