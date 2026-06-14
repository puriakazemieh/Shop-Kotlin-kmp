package com.kazemieh.network.admin

import com.kazemieh.network.admin.dto.request.*
import com.kazemieh.network.admin.dto.response.*

import com.kazemieh.network.common.PageResponse
import com.kazemieh.network.admin.dto.*
import com.kazemieh.network.admin.dto.*

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

    // ---------- Videos ----------
    suspend fun addVideo(productId: Long, bytes: ByteArray, sortOrder: Int? = null): AdminProductVideoResponse
    suspend fun deleteVideo(productId: Long, videoId: Long)

    // ---------- Variants ----------
    suspend fun createVariant(productId: Long, request: AdminCreateVariantRequest): AdminVariantResponse
    suspend fun updateVariant(variantId: Long, request: AdminUpdateVariantRequest): AdminVariantResponse
    suspend fun deleteVariant(variantId: Long)

    // ---------- Inventory ----------
    suspend fun getInventory(variantId: Long): AdminInventoryResponse
    suspend fun setInventory(variantId: Long, request: AdminInventorySetRequest): AdminInventoryResponse
    suspend fun adjustInventory(variantId: Long, request: AdminInventoryAdjustRequest): AdminInventoryResponse

    // ---------- Options ----------
    suspend fun listOptions(): List<AdminOptionResponse>
    suspend fun createOptionType(request: AdminOptionTypeRequest): AdminOptionResponse
    suspend fun updateOptionType(id: Long, request: AdminOptionTypeRequest): AdminOptionResponse
    suspend fun deleteOptionType(id: Long)
    suspend fun createOptionValue(request: AdminOptionValueRequest): AdminOptionValueResponse
    suspend fun updateOptionValue(id: Long, request: AdminOptionValueRequest): AdminOptionValueResponse
    suspend fun deleteOptionValue(id: Long)

    // ---------- Orders ----------
    suspend fun listOrders(
        status: String?,
        userId: Long?,
        page: Int,
        size: Int
    ): PageResponse<AdminOrderSummaryResponse>

    suspend fun getOrderDetail(id: Long): AdminOrderDetailResponse

    suspend fun updateOrderStatus(id: Long, request: AdminUpdateOrderStatusRequest)

    // ---------- Discounts ----------
    suspend fun listDiscounts(): List<AdminDiscountResponse>
    suspend fun createDiscount(request: AdminCreateDiscountRequest): AdminDiscountResponse
    suspend fun updateDiscount(id: Long, request: AdminUpdateDiscountRequest): AdminDiscountResponse
    suspend fun deleteDiscount(id: Long)

    // ---------- Interactions ----------
    suspend fun listReviews(
        productId: Long?,
        isNew: Boolean?,
        page: Int,
        size: Int
    ): PageResponse<AdminInteractionResponse>

    suspend fun listQuestions(
        productId: Long?,
        isNew: Boolean?,
        page: Int,
        size: Int
    ): PageResponse<AdminInteractionResponse>
}
