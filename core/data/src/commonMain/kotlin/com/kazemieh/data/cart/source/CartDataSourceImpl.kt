package com.kazemieh.data.cart.source

import com.kazemieh.network.cart.CartApi
import com.kazemieh.network.cart.dto.request.*
import com.kazemieh.network.cart.dto.response.*
import com.kazemieh.domain.cart.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*




class CartDataSourceImpl(private val api: CartApi) : CartDataSource {
    override suspend fun getCart(): AppResult<CartResponse> = safeApiCall { api.getCart() }
    override suspend fun addItem(request: AddCartItemRequest): AppResult<CartResponse> = safeApiCall { api.addItem(request) }
    override suspend fun updateQty(itemId: Long, request: UpdateCartItemRequest): AppResult<CartResponse> = safeApiCall { api.updateQty(itemId, request) }
    override suspend fun remove(itemId: Long): AppResult<CartResponse> = safeApiCall { api.remove(itemId) }
    override suspend fun clear(): AppResult<Unit> = safeApiCall { api.clear() }
    override suspend fun setVariantQty(variantId: Long, request: SetCartVariantQtyRequest): AppResult<CartResponse> = safeApiCall { api.setVariantQty(variantId, request) }
    override suspend fun adjustVariantQty(variantId: Long, request: AdjustCartVariantQtyRequest): AppResult<CartResponse> = safeApiCall { api.adjustVariantQty(variantId, request) }
}
