package com.kazemieh.network.admin.dto.response

import com.kazemieh.network.catalog.dto.response.ProductAttributeDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminProductResponse(
    val id: Long = 0,
    val categoryId: Long? = null,
    val title: String = "",
    val slug: String = "",
    val description: String? = null,
    val brand: String? = null,
    val attributes: List<ProductAttributeDto> = emptyList(),
    val basePrice: Double? = null,
    val discountedPrice: Double? = null,
    val sku: String? = null,
    val initialOnHand: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerialName("active")
    val isActive: Boolean = true
)
