package com.kazemieh.network

import com.kazemieh.network.dto.PageResponse
import com.kazemieh.network.dto.admin.request.*
import com.kazemieh.network.dto.admin.response.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AdminApiImpl(
    private val client: HttpClient
) : AdminApi {

    override suspend fun listCategories(): List<AdminCategoryResponse> =
        client.get("api/admin/categories").body()

    override suspend fun createCategory(request: AdminCreateCategoryRequest): AdminCategoryResponse =
        client.post("api/admin/categories") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun updateCategory(id: Long, request: AdminUpdateCategoryRequest): AdminCategoryResponse =
        client.patch("api/admin/categories/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun deleteCategory(id: Long) {
        client.delete("api/admin/categories/$id")
    }

    override suspend fun listProducts(
        page: Int,
        size: Int,
        includeInactive: Boolean
    ): PageResponse<AdminProductResponse> =
        client.get("api/admin/products") {
            parameter("page", page)
            parameter("size", size)
            parameter("includeInactive", includeInactive)
        }.body()

    override suspend fun createProduct(request: AdminCreateProductRequest): AdminProductResponse =
        client.post("api/admin/products") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun getProductDetail(id: Long): AdminProductDetailResponse =
        client.get("api/admin/products/$id").body()

    override suspend fun updateProduct(id: Long, request: AdminUpdateProductRequest): AdminProductResponse =
        client.patch("api/admin/products/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun deleteProduct(id: Long) {
        client.delete("api/admin/products/$id")
    }

    override suspend fun addImage(
        productId: Long,
        request: AdminAddImageRequest
    ): AdminProductImageResponse =
        client.post("api/admin/products/$productId/images") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun reorderImages(
        productId: Long,
        request: AdminReorderImagesRequest
    ): List<AdminProductImageResponse> =
        client.patch("api/admin/products/$productId/images/reorder") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun deleteImage(productId: Long, imageId: Long) {
        client.delete("api/admin/products/$productId/images/$imageId")
    }

    override suspend fun createVariant(
        productId: Long,
        request: AdminCreateVariantRequest
    ): AdminVariantResponse =
        client.post("api/admin/products/$productId/variants") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun updateVariant(
        variantId: Long,
        request: AdminUpdateVariantRequest
    ): AdminVariantResponse =
        client.patch("api/admin/variants/$variantId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun getInventory(variantId: Long): AdminInventoryResponse =
        client.get("api/admin/variants/$variantId/inventory").body()

    override suspend fun setInventory(
        variantId: Long,
        request: AdminInventorySetRequest
    ): AdminInventoryResponse =
        client.put("api/admin/variants/$variantId/inventory") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun adjustInventory(
        variantId: Long,
        request: AdminInventoryAdjustRequest
    ): AdminInventoryResponse =
        client.patch("api/admin/variants/$variantId/inventory/adjust") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
