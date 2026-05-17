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
    suspend fun listSizes(): List<AdminSizeResponse>
    suspend fun createSize(request: AdminCreateSizeRequest): AdminSizeResponse
    suspend fun updateSize(id: Long, request: AdminUpdateSizeRequest): AdminSizeResponse
    suspend fun deleteSize(id: Long)

    suspend fun listColors(): List<AdminColorResponse>
    suspend fun createColor(request: AdminCreateColorRequest): AdminColorResponse
    suspend fun updateColor(id: Long, request: AdminUpdateColorRequest): AdminColorResponse
    suspend fun deleteColor(id: Long)

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
}
