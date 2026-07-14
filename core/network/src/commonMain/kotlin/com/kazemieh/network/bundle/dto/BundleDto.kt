package com.kazemieh.network.bundle.dto

import com.kazemieh.network.catalog.dto.response.ProductSummaryResponse
import kotlinx.serialization.Serializable

@Serializable
data class BundleSummaryResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String? = null,
    val product: ProductSummaryResponse,
    val memberCount: Int
)

@Serializable
data class BundleDetailResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String? = null,
    val product: ProductSummaryResponse,
    val members: List<ProductSummaryResponse> = emptyList()
)

@Serializable
data class AdminBundleResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String? = null,
    val productId: Long,
    val memberProductIds: List<Long> = emptyList(),
    val isActive: Boolean
)

@Serializable
data class AdminCreateBundleRequestDto(
    val title: String,
    val slug: String,
    val description: String? = null,
    val productId: Long,
    val memberProductIds: List<Long> = emptyList(),
    val isActive: Boolean = true
)

@Serializable
data class AdminUpdateBundleRequestDto(
    val title: String? = null,
    val description: String? = null,
    val memberProductIds: List<Long>? = null,
    val isActive: Boolean? = null
)

@Serializable
data class IdResponse(val id: Long)
