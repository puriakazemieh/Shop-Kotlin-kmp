package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminProductDetailResponse(
    val product: AdminProductResponse,
    val images: List<AdminProductImageResponse>,
    val videos: List<AdminProductVideoResponse> = emptyList(),
    val variants: List<AdminVariantResponse>
)
