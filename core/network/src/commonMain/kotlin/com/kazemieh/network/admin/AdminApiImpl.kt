package com.kazemieh.network.admin

import com.kazemieh.network.admin.dto.request.*
import com.kazemieh.network.admin.dto.response.*

import com.kazemieh.network.common.safeApiCallRaw

import com.kazemieh.network.common.PageResponse
import com.kazemieh.network.admin.dto.*
import com.kazemieh.network.admin.dto.*
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
                        append(HttpHeaders.ContentType, "image/jpeg")
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

    override suspend fun addVideo(
        productId: Long,
        bytes: ByteArray,
        sortOrder: Int?
    ): AdminProductVideoResponse = safeApiCallRaw {
        client.post("api/admin/products/$productId/videos") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("file", bytes, Headers.build {
                        append(HttpHeaders.ContentType, "video/mp4")
                        append(HttpHeaders.ContentDisposition, "filename=\"video.mp4\"")
                    })
                    if (sortOrder != null) {
                        append("sortOrder", sortOrder.toString())
                    }
                }
            ))
        }
    }

    override suspend fun deleteVideo(productId: Long, videoId: Long) = safeApiCallRaw<Unit> {
        client.delete("api/admin/products/$productId/videos/$videoId")
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

    override suspend fun listOptions(): List<AdminOptionResponse> = safeApiCallRaw {
        client.get("api/admin/options")
    }

    override suspend fun createOptionType(request: AdminOptionTypeRequest): AdminOptionResponse = safeApiCallRaw {
        client.post("api/admin/options/types") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun updateOptionType(id: Long, request: AdminOptionTypeRequest): AdminOptionResponse = safeApiCallRaw {
        client.put("api/admin/options/types/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun deleteOptionType(id: Long) = safeApiCallRaw<Unit> {
        client.delete("api/admin/options/types/$id")
    }

    override suspend fun createOptionValue(request: AdminOptionValueRequest): AdminOptionValueResponse = safeApiCallRaw {
        client.post("api/admin/options/values") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun updateOptionValue(id: Long, request: AdminOptionValueRequest): AdminOptionValueResponse = safeApiCallRaw {
        client.put("api/admin/options/values/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun deleteOptionValue(id: Long) = safeApiCallRaw<Unit> {
        client.delete("api/admin/options/values/$id")
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

    override suspend fun listOrders(
        status: String?,
        userId: Long?,
        page: Int,
        size: Int
    ): PageResponse<AdminOrderSummaryResponse> = safeApiCallRaw {
        client.get("api/admin/orders") {
            parameter("status", status)
            parameter("userId", userId)
            parameter("page", page)
            parameter("size", size)
        }
    }

    override suspend fun getOrderDetail(id: Long): AdminOrderDetailResponse = safeApiCallRaw {
        client.get("api/admin/orders/$id")
    }

    override suspend fun updateOrderStatus(id: Long, request: AdminUpdateOrderStatusRequest) =
        safeApiCallRaw<Unit> {
            client.patch("api/admin/orders/$id/status") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    override suspend fun createDiscount(request: AdminCreateDiscountRequest): AdminDiscountResponse = safeApiCallRaw {
        client.post("api/admin/discounts") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun listDiscounts(): List<AdminDiscountResponse> = safeApiCallRaw {
        client.get("api/admin/discounts")
    }

    override suspend fun updateDiscount(
        id: Long,
        request: AdminUpdateDiscountRequest
    ): AdminDiscountResponse = safeApiCallRaw {
        client.put("api/admin/discounts/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun deleteDiscount(id: Long) = safeApiCallRaw<Unit> {
        client.delete("api/admin/discounts/$id")
    }

    override suspend fun listReviews(
        productId: Long?,
        isNew: Boolean?,
        page: Int,
        size: Int
    ): PageResponse<AdminInteractionResponse> = safeApiCallRaw {
        client.get("api/admin/reviews") {
            parameter("productId", productId)
            parameter("isNew", isNew)
            parameter("page", page)
            parameter("size", size)
        }
    }

    override suspend fun listQuestions(
        productId: Long?,
        isNew: Boolean?,
        page: Int,
        size: Int
    ): PageResponse<AdminInteractionResponse> = safeApiCallRaw {
        client.get("api/admin/questions") {
            parameter("productId", productId)
            parameter("isNew", isNew)
            parameter("page", page)
            parameter("size", size)
        }
    }

    override suspend fun searchWalletUsers(query: String): List<AdminWalletUserResponse> = safeApiCallRaw {
        client.get("api/admin/wallet/users/search") {
            parameter("query", query)
        }
    }

    override suspend fun adjustWalletBalance(request: AdminWalletAdjustRequest) = safeApiCallRaw<Unit> {
        client.post("api/admin/wallet/adjust") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun listWithdrawals(status: String?): List<AdminWithdrawalResponse> = safeApiCallRaw {
        client.get("api/admin/wallet/withdrawals") {
            parameter("status", status)
        }
    }

    override suspend fun processWithdrawal(id: Long, request: AdminProcessWithdrawalRequest) = safeApiCallRaw<Unit> {
        client.post("api/admin/wallet/withdrawals/$id/process") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
