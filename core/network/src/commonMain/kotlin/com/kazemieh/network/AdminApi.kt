package com.kazemieh.network

import com.kazemieh.network.dto.PageResponse
import com.kazemieh.network.dto.admin.request.*
import com.kazemieh.network.dto.admin.response.*
import com.kazemieh.network.dto.catalog.response.ColorResponse
import com.kazemieh.network.dto.catalog.response.SizeResponse

interface AdminApi {

    // ---------- Categories ----------
    suspend fun listCategories(): List<AdminCategoryResponse>
    suspend fun createCategory(request: AdminCreateCategoryRequest): AdminCategoryResponse
    suspend fun updateCategory(id: Long, request: AdminUpdateCategoryRequest): AdminCategoryResponse
    suspend fun deleteCategory(id: Long)

    // ---------- Products ----------
    suspend fun listProducts(page: Int, size: Int, includeInactive: Boolean, query: String? = null): PageResponse<AdminProductResponse>
    suspend fun createProduct(request: AdminCreateProductRequest): AdminProductResponse
    suspend fun getProductDetail(id: Long): AdminProductDetailResponse
    suspend fun updateProduct(id: Long, request: AdminUpdateProductRequest): AdminProductResponse
    suspend fun deleteProduct(id: Long)

    // ---------- Images ----------
    suspend fun addImage(productId: Long, bytes: ByteArray, sortOrder: Int? = null): AdminProductImageResponse
    suspend fun reorderImages(productId: Long, request: AdminReorderImagesRequest): List<AdminProductImageResponse>
    suspend fun deleteImage(productId: Long, imageId: Long)

    // ---------- Variants ----------
    suspend fun createVariant(productId: Long, request: AdminCreateVariantRequest): AdminVariantResponse
    suspend fun updateVariant(variantId: Long, request: AdminUpdateVariantRequest): AdminVariantResponse
    suspend fun deleteVariant(variantId: Long)

    // ---------- Inventory ----------
    suspend fun getInventory(variantId: Long): AdminInventoryResponse
    suspend fun setInventory(variantId: Long, request: AdminInventorySetRequest): AdminInventoryResponse
    suspend fun adjustInventory(variantId: Long, request: AdminInventoryAdjustRequest): AdminInventoryResponse

    // ---------- Orders ----------
    suspend fun listOrders(
        status: String?,
        userId: Long?,
        page: Int,
        size: Int
    ): PageResponse<AdminOrderSummaryResponse>

    suspend fun getOrderDetail(id: Long): AdminOrderDetailResponse

    suspend fun updateOrderStatus(id: Long, request: AdminUpdateOrderStatusRequest)
}
