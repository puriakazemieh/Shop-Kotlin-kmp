package com.kazemieh.domain.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.*

interface AdminRepository {
    // Categories
    suspend fun listCategories(): AppResult<List<AdminCategory>>
    suspend fun createCategory(name: String, slug: String, parentId: Long?): AppResult<AdminCategory>
    suspend fun updateCategory(id: Long, name: String?, slug: String?, parentId: Long?): AppResult<AdminCategory>
    suspend fun deleteCategory(id: Long): AppResult<Unit>

    // Sizes
    suspend fun listSizes(): AppResult<List<Size>>
    suspend fun createSize(name: String, sortOrder: Int): AppResult<Size>
    suspend fun updateSize(id: Long, name: String?, sortOrder: Int?): AppResult<Size>
    suspend fun deleteSize(id: Long): AppResult<Unit>

    // Colors
    suspend fun listColors(): AppResult<List<Color>>
    suspend fun createColor(name: String, hex: String?): AppResult<Color>
    suspend fun updateColor(id: Long, name: String?, hex: String?): AppResult<Color>
    suspend fun deleteColor(id: Long): AppResult<Unit>

    // Products
    suspend fun listProducts(page: Int, size: Int, includeInactive: Boolean, query: String? = null): AppResult<AdminPage<AdminProduct>>
    suspend fun createProduct(
        categoryId: Long?,
        title: String,
        slug: String,
        description: String?,
        basePrice: Double?,
        isActive: Boolean,
        variants: List<AdminCreateVariant>? = null
    ): AppResult<AdminProduct>
    suspend fun getProductDetail(id: Long): AppResult<AdminProductDetail>
    suspend fun updateProduct(id: Long, categoryId: Long?, title: String?, slug: String?, description: String?, basePrice: Double?, isActive: Boolean?): AppResult<AdminProduct>
    suspend fun deleteProduct(id: Long): AppResult<Unit>

    // Images
    suspend fun addImage(productId: Long, bytes: ByteArray, sortOrder: Int?): AppResult<AdminProductImage>
    suspend fun reorderImages(productId: Long, items: List<Pair<Long, Int>>): AppResult<List<AdminProductImage>>
    suspend fun deleteImage(productId: Long, imageId: Long): AppResult<Unit>

    // Variants
    suspend fun createVariant(productId: Long, sizeId: Long, colorId: Long, sku: String, price: Double, compareAtPrice: Double?, isActive: Boolean, initialOnHand: Int): AppResult<AdminVariant>
    suspend fun updateVariant(variantId: Long, sku: String?, price: Double?, compareAtPrice: Double?, sizeId: Long?, colorId: Long?, isActive: Boolean?): AppResult<AdminVariant>
    suspend fun deleteVariant(variantId: Long): AppResult<Unit>

    // Inventory
    suspend fun getInventory(variantId: Long): AppResult<AdminInventory>
    suspend fun setInventory(variantId: Long, onHand: Int, version: Int?): AppResult<AdminInventory>
    suspend fun adjustInventory(variantId: Long, delta: Int, version: Int?): AppResult<AdminInventory>
}
