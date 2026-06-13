package com.kazemieh.data.cart.repository

import com.kazemieh.network.cart.dto.request.*
import com.kazemieh.network.cart.dto.response.*
import com.kazemieh.domain.cart.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*
import com.kazemieh.data.cart.source.CartDataSource
import com.kazemieh.data.cart.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow





class CartRepositoryImpl(
    private val dataSource: CartDataSource
) : CartRepository {

    override fun getCart(): Flow<AppResult<Cart>> = flow {
        emit(AppResult.Loading)
        emit(dataSource.getCart().map { it.toDomain() })
    }

    override suspend fun addItem(variantId: Long, qty: Int): AppResult<Cart> {
        val result = dataSource.addItem(AddCartItemRequest(variantId, qty)).map { it.toDomain() }
        if (result is AppResult.Success) {
            CartEventBus.refresh()
        }
        return result
    }

    override suspend fun updateQty(itemId: Long, qty: Int): AppResult<Cart> {
        val result = dataSource.updateQty(itemId, UpdateCartItemRequest(qty)).map { it.toDomain() }
        if (result is AppResult.Success) {
            CartEventBus.refresh()
        }
        return result
    }

    override suspend fun remove(itemId: Long): AppResult<Cart> {
        val result = dataSource.remove(itemId).map { it.toDomain() }
        if (result is AppResult.Success) {
            CartEventBus.refresh()
        }
        return result
    }

    override suspend fun clear(): AppResult<Unit> {
        val result = dataSource.clear()
        if (result is AppResult.Success) {
            CartEventBus.refresh()
        }
        return result
    }

    override suspend fun setVariantQty(variantId: Long, qty: Int): AppResult<Cart> {
        val result = dataSource.setVariantQty(variantId, SetCartVariantQtyRequest(qty)).map { it.toDomain() }
        if (result is AppResult.Success) {
            CartEventBus.refresh()
        }
        return result
    }

    override suspend fun adjustVariantQty(variantId: Long, delta: Int): AppResult<Cart> {
        val result = dataSource.adjustVariantQty(variantId, AdjustCartVariantQtyRequest(delta)).map { it.toDomain() }
        if (result is AppResult.Success) {
            CartEventBus.refresh()
        }
        return result
    }

    override suspend fun moveToSaveForLater(itemId: Long): AppResult<Cart> {
        val result = dataSource.moveToSaveForLater(itemId).map { it.toDomain() }
        if (result is AppResult.Success) {
            CartEventBus.refresh()
        }
        return result
    }

    override suspend fun moveToCart(itemId: Long): AppResult<Cart> {
        val result = dataSource.moveToCart(itemId).map { it.toDomain() }
        if (result is AppResult.Success) {
            CartEventBus.refresh()
        }
        return result
    }
}
