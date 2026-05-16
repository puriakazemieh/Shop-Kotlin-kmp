package com.kazemieh.data.admin.source

import com.kazemieh.common.AppResult
import com.kazemieh.network.dto.PageResponse
import com.kazemieh.network.dto.admin.request.*
import com.kazemieh.network.dto.admin.response.*

interface AdminDataSource {
    suspend fun listCategories(): AppResult<List<AdminCategoryResponse>>
    suspend fun createCategory(request: AdminCreateCategoryRequest): AppResult<AdminCategoryResponse>
    suspend fun updateCategory(id: Long, request: AdminUpdateCategoryRequest): AppResult<AdminCategoryResponse>
    suspend fun deleteCategory(id: Long): AppResult<Unit>

    suspend fun listProducts(page: Int, size: Int, includeInactive: Boolean): AppResult<PageResponse<AdminProductResponse>>
    suspend fun createProduct(request: AdminCreateProductRequest): AppResult<AdminProductResponse>
    suspend fun getProductDetail(id: Long): AppResult<AdminProductDetailResponse>
    suspend fun updateProduct(id: Long, request: AdminUpdateProductRequest): AppResult<AdminProductResponse>
    suspend fun deleteProduct(id: Long): AppResult<Unit>

    suspend fun addImage(productId: Long, request: AdminAddImageRequest): AppResult<AdminProductImageResponse>
    suspend fun deleteImage(productId: Long, imageId: Long): AppResult<Unit>

    suspend fun createVariant(productId: Long, request: AdminCreateVariantRequest): AppResult<AdminVariantResponse>
    suspend fun updateVariant(variantId: Long, request: AdminUpdateVariantRequest): AppResult<AdminVariantResponse>

    suspend fun getInventory(variantId: Long): AppResult<AdminInventoryResponse>
    suspend fun setInventory(variantId: Long, request: AdminInventorySetRequest): AppResult<AdminInventoryResponse>
    suspend fun adjustInventory(variantId: Long, request: AdminInventoryAdjustRequest): AppResult<AdminInventoryResponse>
}
