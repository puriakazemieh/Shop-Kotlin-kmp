package com.kazemieh.data.cart.repository

import com.kazemieh.network.cart.dto.request.*
import com.kazemieh.network.cart.dto.response.*
import com.kazemieh.domain.cart.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*
import com.kazemieh.data.cart.source.CartDataSource
import com.kazemieh.data.cart.mapper.toDomain
import com.kazemieh.data.common.guardFlow
import com.kazemieh.data.common.guardOrError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.kazemieh.config.capabilities.AssetUrlResolver
import com.kazemieh.config.capabilities.FeatureUseCaseGuard

class CartRepositoryImpl(
    private val dataSource: CartDataSource,
    private val assetUrlResolver: AssetUrlResolver,
    private val guard: FeatureUseCaseGuard? = null
) : CartRepository {

    override fun getCart(): Flow<AppResult<Cart>> = guard.guardFlow("commerce.core") {
        flow {
            emit(AppResult.Loading)
            emit(dataSource.getCart().map { it.toDomain(assetUrlResolver) })
        }
    }

    override suspend fun addItem(productId: Long?, variantId: Long?, qty: Int): AppResult<Cart> =
        guard.guardOrError("commerce.core") {
            val result = dataSource.addItem(AddCartItemRequest(productId, variantId, qty)).map { it.toDomain(assetUrlResolver) }
            if (result is AppResult.Success) {
                CartEventBus.refresh()
            }
            result
        }

    override suspend fun updateQty(itemId: Long, qty: Int): AppResult<Cart> =
        guard.guardOrError("commerce.core") {
            val result = dataSource.updateQty(itemId, UpdateCartItemRequest(qty)).map { it.toDomain(assetUrlResolver) }
            if (result is AppResult.Success) {
                CartEventBus.refresh()
            }
            result
        }

    override suspend fun remove(itemId: Long): AppResult<Cart> =
        guard.guardOrError("commerce.core") {
            val result = dataSource.remove(itemId).map { it.toDomain(assetUrlResolver) }
            if (result is AppResult.Success) {
                CartEventBus.refresh()
            }
            result
        }

    override suspend fun clear(): AppResult<Unit> =
        guard.guardOrError("commerce.core") {
            val result = dataSource.clear()
            if (result is AppResult.Success) {
                CartEventBus.refresh()
            }
            result
        }

    override suspend fun setVariantQty(variantId: Long, qty: Int): AppResult<Cart> =
        guard.guardOrError("commerce.core") {
            val result = dataSource.setVariantQty(variantId, SetCartVariantQtyRequest(qty)).map { it.toDomain(assetUrlResolver) }
            if (result is AppResult.Success) {
                CartEventBus.refresh()
            }
            result
        }

    override suspend fun adjustVariantQty(variantId: Long, delta: Int): AppResult<Cart> =
        guard.guardOrError("commerce.core") {
            val result = dataSource.adjustVariantQty(variantId, AdjustCartVariantQtyRequest(delta)).map { it.toDomain(assetUrlResolver) }
            if (result is AppResult.Success) {
                CartEventBus.refresh()
            }
            result
        }

    override suspend fun moveToSaveForLater(itemId: Long): AppResult<Cart> =
        guard.guardOrError("commerce.core") {
            val result = dataSource.moveToSaveForLater(itemId).map { it.toDomain(assetUrlResolver) }
            if (result is AppResult.Success) {
                CartEventBus.refresh()
            }
            result
        }

    override suspend fun moveToCart(itemId: Long): AppResult<Cart> =
        guard.guardOrError("commerce.core") {
            val result = dataSource.moveToCart(itemId).map { it.toDomain(assetUrlResolver) }
            if (result is AppResult.Success) {
                CartEventBus.refresh()
            }
            result
        }

    override suspend fun applyDiscount(code: String): AppResult<Cart> =
        guard.guardOrError("commerce.core") {
            val result = dataSource.applyDiscount(ApplyDiscountRequest(code)).map { it.toDomain(assetUrlResolver) }
            if (result is AppResult.Success) {
                CartEventBus.refresh()
            }
            result
        }

    override suspend fun removeDiscount(): AppResult<Cart> =
        guard.guardOrError("commerce.core") {
            val result = dataSource.removeDiscount().map { it.toDomain(assetUrlResolver) }
            if (result is AppResult.Success) {
                CartEventBus.refresh()
            }
            result
        }
}
