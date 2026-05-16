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

    // ---------- Sizes & Colors ----------
    suspend fun createSize(request: AdminCreateSizeRequest): SizeResponse
    suspend fun createColor(request: AdminCreateColorRequest): ColorResponse

    // ---------- Products ----------
    suspend fun listProducts(page: Int, size: Int, includeInactive: Boolean, query: String? = null): PageResponse<AdminProductResponse>
    suspend fun createProduct(request: AdminCreateProductRequest): AdminProductResponse
    suspend fun getProductDetail(id: Long): AdminProductDetailResponse
    suspend fun updateProduct(id: Long, request: AdminUpdateProductRequest): AdminProductResponse
    suspend fun deleteProduct(id: Long)

    // ---------- Images ----------
    suspend fun addImage(productId: Long, request: AdminAddImageRequest): AdminProductImageResponse
    suspend fun reorderImages(productId: Long, request: AdminReorderImagesRequest): List<AdminProductImageResponse>
    suspend fun deleteImage(productId: Long, imageId: Long)

    // ---------- Variants ----------
    suspend fun createVariant(productId: Long, request: AdminCreateVariantRequest): AdminVariantResponse
    suspend fun updateVariant(variantId: Long, request: AdminUpdateVariantRequest): AdminVariantResponse

    // ---------- Inventory ----------
    suspend fun getInventory(variantId: Long): AdminInventoryResponse
    suspend fun setInventory(variantId: Long, request: AdminInventorySetRequest): AdminInventoryResponse
    suspend fun adjustInventory(variantId: Long, request: AdminInventoryAdjustRequest): AdminInventoryResponse
}
