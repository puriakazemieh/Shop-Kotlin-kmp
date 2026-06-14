package com.kazemieh.data.admin.source

import com.kazemieh.network.admin.dto.request.*
import com.kazemieh.network.admin.dto.response.*
import com.kazemieh.domain.admin.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*





interface AdminDataSource {
    suspend fun listCategories(): AppResult<List<AdminCategoryResponse>>
    suspend fun createCategory(request: AdminCreateCategoryRequest): AppResult<AdminCategoryResponse>
    suspend fun updateCategory(id: Long, request: AdminUpdateCategoryRequest): AppResult<AdminCategoryResponse>
    suspend fun deleteCategory(id: Long): AppResult<Unit>

    suspend fun listProducts(page: Int, size: Int, includeInactive: Boolean, query: String? = null): AppResult<PageResponse<AdminProductResponse>>
    suspend fun createProduct(request: AdminCreateProductRequest): AppResult<AdminProductResponse>
    suspend fun getProductDetail(id: Long): AppResult<AdminProductDetailResponse>
    suspend fun updateProduct(id: Long, request: AdminUpdateProductRequest): AppResult<AdminProductResponse>
    suspend fun deleteProduct(id: Long): AppResult<Unit>

    suspend fun addImage(productId: Long, bytes: ByteArray, sortOrder: Int? = null): AppResult<AdminProductImageResponse>
    suspend fun reorderImages(productId: Long, request: AdminReorderImagesRequest): AppResult<List<AdminProductImageResponse>>
    suspend fun deleteImage(productId: Long, imageId: Long): AppResult<Unit>

    suspend fun addVideo(productId: Long, bytes: ByteArray, sortOrder: Int? = null): AppResult<AdminProductVideoResponse>
    suspend fun deleteVideo(productId: Long, videoId: Long): AppResult<Unit>

    suspend fun createVariant(productId: Long, request: AdminCreateVariantRequest): AppResult<AdminVariantResponse>
    suspend fun updateVariant(variantId: Long, request: AdminUpdateVariantRequest): AppResult<AdminVariantResponse>
    suspend fun deleteVariant(variantId: Long): AppResult<Unit>

    suspend fun listOptions(): AppResult<List<AdminOptionResponse>>
    suspend fun createOptionType(request: AdminOptionTypeRequest): AppResult<AdminOptionResponse>
    suspend fun updateOptionType(id: Long, request: AdminOptionTypeRequest): AppResult<AdminOptionResponse>
    suspend fun deleteOptionType(id: Long): AppResult<Unit>
    suspend fun createOptionValue(request: AdminOptionValueRequest): AppResult<AdminOptionValueResponse>
    suspend fun updateOptionValue(id: Long, request: AdminOptionValueRequest): AppResult<AdminOptionValueResponse>
    suspend fun deleteOptionValue(id: Long): AppResult<Unit>

    suspend fun getInventory(variantId: Long): AppResult<AdminInventoryResponse>
    suspend fun setInventory(variantId: Long, request: AdminInventorySetRequest): AppResult<AdminInventoryResponse>
    suspend fun adjustInventory(variantId: Long, request: AdminInventoryAdjustRequest): AppResult<AdminInventoryResponse>

    // Orders
    suspend fun listOrders(
        status: String?,
        userId: Long?,
        page: Int,
        size: Int
    ): AppResult<PageResponse<AdminOrderSummaryResponse>>

    suspend fun getOrderDetail(id: Long): AppResult<AdminOrderDetailResponse>

    suspend fun updateOrderStatus(id: Long, request: AdminUpdateOrderStatusRequest): AppResult<Unit>

    // Discounts
    suspend fun listDiscounts(): AppResult<List<AdminDiscountResponse>>
    suspend fun createDiscount(request: AdminCreateDiscountRequest): AppResult<AdminDiscountResponse>
    suspend fun updateDiscount(id: Long, request: AdminUpdateDiscountRequest): AppResult<AdminDiscountResponse>
    suspend fun deleteDiscount(id: Long): AppResult<Unit>

    // Interactions
    suspend fun listReviews(
        productId: Long?,
        isNew: Boolean?,
        page: Int,
        size: Int
    ): AppResult<PageResponse<AdminInteractionResponse>>

    suspend fun listQuestions(
        productId: Long?,
        isNew: Boolean?,
        page: Int,
        size: Int
    ): AppResult<PageResponse<AdminInteractionResponse>>
}
