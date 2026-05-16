package com.kazemieh.network.dto.admin.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminProductDetailResponse(
    val product: AdminProductResponse,
    val images: List<AdminProductImageResponse>,
    val variants: List<AdminVariantResponse>
)
