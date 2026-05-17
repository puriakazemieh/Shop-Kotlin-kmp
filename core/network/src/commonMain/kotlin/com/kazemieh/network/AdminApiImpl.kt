package com.kazemieh.network

import com.kazemieh.network.dto.PageResponse
import com.kazemieh.network.dto.admin.request.*
import com.kazemieh.network.dto.admin.response.*
import com.kazemieh.network.dto.catalog.response.ColorResponse
import com.kazemieh.network.dto.catalog.response.SizeResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

class AdminApiImpl(
    private val client: HttpClient
) : AdminApi {

    override suspend fun listCategories(): List<AdminCategoryResponse> = safeApiCallRaw {
        client.get("api/admin/categories")
    }

    override suspend fun createCategory(request: AdminCreateCategoryRequest): AdminCategoryResponse = safeApiCallRaw {
        client.post("api/admin/categories") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun updateCategory(id: Long, request: AdminUpdateCategoryRequest): AdminCategoryResponse = safeApiCallRaw {
        client.patch("api/admin/categories/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun deleteCategory(id: Long) = safeApiCallRaw<Unit> {
        client.delete("api/admin/categories/$id")
    }

    override suspend fun listSizes(): List<AdminSizeResponse> = safeApiCallRaw {
        client.get("api/admin/sizes")
    }

    override suspend fun createSize(request: AdminCreateSizeRequest): AdminSizeResponse = safeApiCallRaw {
        client.post("api/admin/sizes") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun updateSize(id: Long, request: AdminUpdateSizeRequest): AdminSizeResponse = safeApiCallRaw {
        client.patch("api/admin/sizes/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun deleteSize(id: Long) = safeApiCallRaw<Unit> {
        client.delete("api/admin/sizes/$id")
    }

    override suspend fun listColors(): List<AdminColorResponse> = safeApiCallRaw {
        client.get("api/admin/colors")
    }

    override suspend fun createColor(request: AdminCreateColorRequest): AdminColorResponse = safeApiCallRaw {
        client.post("api/admin/colors") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun updateColor(id: Long, request: AdminUpdateColorRequest): AdminColorResponse = safeApiCallRaw {
        client.patch("api/admin/colors/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun deleteColor(id: Long) = safeApiCallRaw<Unit> {
        client.delete("api/admin/colors/$id")
    }

    override suspend fun listProducts(
        page: Int,
        size: Int,
        includeInactive: Boolean,
        query: String?
    ): PageResponse<AdminProductResponse> = safeApiCallRaw {
        client.get("api/admin/products") {
            parameter("page", page)
            parameter("size", size)
            parameter("includeInactive", includeInactive)
            parameter("q", query)
        }
    }

    override suspend fun createProduct(request: AdminCreateProductRequest): AdminProductResponse = safeApiCallRaw {
        client.post("api/admin/products") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun getProductDetail(id: Long): AdminProductDetailResponse = safeApiCallRaw {
        client.get("api/admin/products/$id")
    }

    override suspend fun updateProduct(id: Long, request: AdminUpdateProductRequest): AdminProductResponse = safeApiCallRaw {
        client.patch("api/admin/products/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun deleteProduct(id: Long) = safeApiCallRaw<Unit> {
        client.delete("api/admin/products/$id")
    }

    override suspend fun addImage(
        productId: Long,
        bytes: ByteArray,
        sortOrder: Int?
    ): AdminProductImageResponse = safeApiCallRaw {
        client.post("api/admin/products/$productId/images") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("file", bytes, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"image.jpg\"")
                    })
                    if (sortOrder != null) {
                        append("sortOrder", sortOrder.toString())
                    }
                }
            ))
        }
    }

    override suspend fun reorderImages(
        productId: Long,
        request: AdminReorderImagesRequest
    ): List<AdminProductImageResponse> = safeApiCallRaw {
        client.patch("api/admin/products/$productId/images/reorder") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun deleteImage(productId: Long, imageId: Long) = safeApiCallRaw<Unit> {
        client.delete("api/admin/products/$productId/images/$imageId")
    }

    override suspend fun createVariant(
        productId: Long,
        request: AdminCreateVariantRequest
    ): AdminVariantResponse = safeApiCallRaw {
        client.post("api/admin/products/$productId/variants") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun updateVariant(
        variantId: Long,
        request: AdminUpdateVariantRequest
    ): AdminVariantResponse = safeApiCallRaw {
        client.patch("api/admin/variants/$variantId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun deleteVariant(variantId: Long) = safeApiCallRaw<Unit> {
        client.delete("api/admin/variants/$variantId")
    }

    override suspend fun getInventory(variantId: Long): AdminInventoryResponse = safeApiCallRaw {
        client.get("api/admin/variants/$variantId/inventory")
    }

    override suspend fun setInventory(
        variantId: Long,
        request: AdminInventorySetRequest
    ): AdminInventoryResponse = safeApiCallRaw {
        client.put("api/admin/variants/$variantId/inventory") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun adjustInventory(
        variantId: Long,
        request: AdminInventoryAdjustRequest
    ): AdminInventoryResponse = safeApiCallRaw {
        client.patch("api/admin/variants/$variantId/inventory/adjust") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
