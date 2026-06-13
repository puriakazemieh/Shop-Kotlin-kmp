package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminCreateDiscountRequest(
    val code: String,
    val type: String, // "PERCENTAGE" or "FIXED_AMOUNT"
    val value: Double,
    val maxDiscountAmount: Double? = null,
    val minOrderAmount: Double? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val usageLimit: Int? = null,
    val isActive: Boolean = true
)
