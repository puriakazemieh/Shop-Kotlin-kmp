package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.*

interface AdminRepository {
    // Categories
    suspend fun listCategories(): AppResult<List<AdminCategory>>
    suspend fun createCategory(name: String, slug: String, parentId: Long?): AppResult<AdminCategory>
    suspend fun updateCategory(id: Long, name: String?, slug: String?, parentId: Long?): AppResult<AdminCategory>
    suspend fun deleteCategory(id: Long): AppResult<Unit>

    // Products
    suspend fun listProducts(page: Int, size: Int, includeInactive: Boolean, query: String? = null): AppResult<AdminPage<AdminProduct>>
    suspend fun createProduct(
        categoryId: Long?,
        title: String,
        slug: String,
        description: String?,
        basePrice: Double?,
        discountedPrice: Double? = null,
        sku: String? = null,
        initialOnHand: Int? = null,
        isActive: Boolean,
        variants: List<AdminCreateVariant>? = null
    ): AppResult<AdminProduct>
    suspend fun getProductDetail(id: Long): AppResult<AdminProductDetail>
    suspend fun updateProduct(
        id: Long,
        categoryId: Long?,
        title: String?,
        slug: String?,
        description: String?,
        basePrice: Double?,
        discountedPrice: Double? = null,
        isActive: Boolean?
    ): AppResult<AdminProduct>
    suspend fun deleteProduct(id: Long): AppResult<Unit>

    // Images
    suspend fun addImage(productId: Long, bytes: ByteArray, sortOrder: Int?): AppResult<AdminProductImage>
    suspend fun reorderImages(productId: Long, items: List<Pair<Long, Int>>): AppResult<List<AdminProductImage>>
    suspend fun deleteImage(productId: Long, imageId: Long): AppResult<Unit>

    // Videos
    suspend fun addVideo(productId: Long, bytes: ByteArray, sortOrder: Int?): AppResult<AdminProductVideo>
    suspend fun deleteVideo(productId: Long, videoId: Long): AppResult<Unit>

    // Variants
    suspend fun createVariant(productId: Long, options: List<AdminVariantOption>, sku: String, price: Double, discountedPrice: Double?, compareAtPrice: Double?, isActive: Boolean, initialOnHand: Int): AppResult<AdminVariant>
    suspend fun updateVariant(variantId: Long, sku: String?, price: Double?, discountedPrice: Double?, compareAtPrice: Double?, options: List<AdminVariantOption>?, isActive: Boolean?): AppResult<AdminVariant>
    suspend fun deleteVariant(variantId: Long): AppResult<Unit>

    // Options
    suspend fun listOptions(): AppResult<List<AdminOption>>
    suspend fun createOptionType(name: String): AppResult<AdminOption>
    suspend fun updateOptionType(id: Long, name: String): AppResult<AdminOption>
    suspend fun deleteOptionType(id: Long): AppResult<Unit>
    suspend fun createOptionValue(optionTypeId: Long, value: String): AppResult<AdminOptionValue>
    suspend fun updateOptionValue(id: Long, optionTypeId: Long, value: String): AppResult<AdminOptionValue>
    suspend fun deleteOptionValue(id: Long): AppResult<Unit>

    // Inventory
    suspend fun getInventory(variantId: Long): AppResult<AdminInventory>
    suspend fun setInventory(variantId: Long, onHand: Int, version: Int?): AppResult<AdminInventory>
    suspend fun adjustInventory(variantId: Long, delta: Int, version: Int?): AppResult<AdminInventory>

    // Orders
    suspend fun listOrders(
        status: String? = null,
        userId: Long? = null,
        page: Int = 0,
        size: Int = 20
    ): AppResult<AdminPage<AdminOrderSummary>>

    suspend fun getOrderDetail(id: Long): AppResult<AdminOrderDetail>

    suspend fun updateOrderStatus(id: Long, status: String): AppResult<Unit>

    // Discounts
    suspend fun listDiscounts(): AppResult<List<Discount>>
    suspend fun createDiscount(param: CreateDiscountParam): AppResult<Discount>
    suspend fun updateDiscount(id: Long, param: UpdateDiscountParam): AppResult<Discount>
    suspend fun deleteDiscount(id: Long): AppResult<Unit>
}
