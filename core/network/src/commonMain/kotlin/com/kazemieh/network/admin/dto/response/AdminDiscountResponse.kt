package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminDiscountResponse(
    val id: Long,
    val code: String,
    val type: String,
    val value: Double,
    val maxDiscountAmount: Double? = null,
    val minOrderAmount: Double? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val usageLimit: Int? = null,
    val usageCount: Int = 0,
    val isActive: Boolean = true
)
