package com.kazemieh.data.admin.source

import com.kazemieh.common.AppResult
import com.kazemieh.network.AdminApi
import com.kazemieh.network.dto.PageResponse
import com.kazemieh.network.dto.admin.request.*
import com.kazemieh.network.dto.admin.response.*
import com.kazemieh.network.safeApiCall

class AdminDataSourceImpl(
    private val api: AdminApi
) : AdminDataSource {

    override suspend fun listCategories(): AppResult<List<AdminCategoryResponse>> = safeApiCall {
        api.listCategories()
    }

    override suspend fun createCategory(request: AdminCreateCategoryRequest): AppResult<AdminCategoryResponse> = safeApiCall {
        api.createCategory(request)
    }

    override suspend fun updateCategory(id: Long, request: AdminUpdateCategoryRequest): AppResult<AdminCategoryResponse> = safeApiCall {
        api.updateCategory(id, request)
    }

    override suspend fun deleteCategory(id: Long): AppResult<Unit> = safeApiCall {
        api.deleteCategory(id)
    }

    override suspend fun listProducts(page: Int, size: Int, includeInactive: Boolean, query: String?): AppResult<PageResponse<AdminProductResponse>> = safeApiCall {
        api.listProducts(page, size, includeInactive, query)
    }

    override suspend fun createProduct(request: AdminCreateProductRequest): AppResult<AdminProductResponse> = safeApiCall {
        api.createProduct(request)
    }

    override suspend fun getProductDetail(id: Long): AppResult<AdminProductDetailResponse> = safeApiCall {
        api.getProductDetail(id)
    }

    override suspend fun updateProduct(id: Long, request: AdminUpdateProductRequest): AppResult<AdminProductResponse> = safeApiCall {
        api.updateProduct(id, request)
    }

    override suspend fun deleteProduct(id: Long): AppResult<Unit> = safeApiCall {
        api.deleteProduct(id)
    }

    override suspend fun addImage(productId: Long, bytes: ByteArray, sortOrder: Int?): AppResult<AdminProductImageResponse> = safeApiCall {
        api.addImage(productId, bytes, sortOrder)
    }

    override suspend fun reorderImages(productId: Long, request: AdminReorderImagesRequest): AppResult<List<AdminProductImageResponse>> = safeApiCall {
        api.reorderImages(productId, request)
    }

    override suspend fun deleteImage(productId: Long, imageId: Long): AppResult<Unit> = safeApiCall {
        api.deleteImage(productId, imageId)
    }

    override suspend fun createVariant(productId: Long, request: AdminCreateVariantRequest): AppResult<AdminVariantResponse> = safeApiCall {
        api.createVariant(productId, request)
    }

    override suspend fun updateVariant(variantId: Long, request: AdminUpdateVariantRequest): AppResult<AdminVariantResponse> = safeApiCall {
        api.updateVariant(variantId, request)
    }

    override suspend fun deleteVariant(variantId: Long): AppResult<Unit> = safeApiCall {
        api.deleteVariant(variantId)
    }

    override suspend fun getInventory(variantId: Long): AppResult<AdminInventoryResponse> = safeApiCall {
        api.getInventory(variantId)
    }

    override suspend fun setInventory(variantId: Long, request: AdminInventorySetRequest): AppResult<AdminInventoryResponse> = safeApiCall {
        api.setInventory(variantId, request)
    }

    override suspend fun adjustInventory(variantId: Long, request: AdminInventoryAdjustRequest): AppResult<AdminInventoryResponse> = safeApiCall {
        api.adjustInventory(variantId, request)
    }

    override suspend fun listOrders(
        status: String?,
        userId: Long?,
        page: Int,
        size: Int
    ): AppResult<PageResponse<AdminOrderSummaryResponse>> = safeApiCall {
        api.listOrders(status, userId, page, size)
    }

    override suspend fun getOrderDetail(id: Long): AppResult<AdminOrderDetailResponse> = safeApiCall {
        api.getOrderDetail(id)
    }

    override suspend fun updateOrderStatus(id: Long, request: AdminUpdateOrderStatusRequest): AppResult<Unit> = safeApiCall {
        api.updateOrderStatus(id, request)
    }
}
