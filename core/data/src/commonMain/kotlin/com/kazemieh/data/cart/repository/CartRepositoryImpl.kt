package com.kazemieh.data.cart.repository

import com.kazemieh.common.AppResult
import com.kazemieh.common.map
import com.kazemieh.data.cart.mapper.toDomain
import com.kazemieh.data.cart.source.CartDataSource
import com.kazemieh.domain.model.Cart
import com.kazemieh.domain.repository.CartRepository
import com.kazemieh.network.dto.cart.request.*
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
        return dataSource.addItem(AddCartItemRequest(variantId, qty)).map { it.toDomain() }
    }

    override suspend fun updateQty(itemId: Long, qty: Int): AppResult<Cart> {
        return dataSource.updateQty(itemId, UpdateCartItemRequest(qty)).map { it.toDomain() }
    }

    override suspend fun remove(itemId: Long): AppResult<Cart> {
        return dataSource.remove(itemId).map { it.toDomain() }
    }

    override suspend fun clear(): AppResult<Unit> {
        return dataSource.clear()
    }

    override suspend fun setVariantQty(variantId: Long, qty: Int): AppResult<Cart> {
        return dataSource.setVariantQty(variantId, SetCartVariantQtyRequest(qty)).map { it.toDomain() }
    }

    override suspend fun adjustVariantQty(variantId: Long, delta: Int): AppResult<Cart> {
        return dataSource.adjustVariantQty(variantId, AdjustCartVariantQtyRequest(delta)).map { it.toDomain() }
    }
}
