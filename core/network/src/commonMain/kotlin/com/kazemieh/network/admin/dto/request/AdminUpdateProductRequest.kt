package com.kazemieh.network.admin.dto.request

import com.kazemieh.network.catalog.dto.response.ProductAttributeDto
import kotlinx.serialization.Serializable

@Serializable
data class AdminUpdateProductRequest(
    val categoryId: Long? = null,
    val title: String? = null,
    val slug: String? = null,
    val description: String? = null,
    val brand: String? = null,
    val attributes: List<ProductAttributeDto>? = null,
    val basePrice: Double? = null,
    val discountedPrice: Double? = null,
    val isActive: Boolean? = null
)
